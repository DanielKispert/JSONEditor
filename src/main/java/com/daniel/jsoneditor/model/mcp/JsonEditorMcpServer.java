package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.util.VersionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * MCP (Model Context Protocol) Server for the JSON Editor.
 * Provides read-only access to the current editor session via HTTP JSON-RPC.
 * Listens only on localhost for security.
 */
public class JsonEditorMcpServer
{
    private static final Logger logger = LoggerFactory.getLogger(JsonEditorMcpServer.class);
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final String PROTOCOL_VERSION = "2024-11-05";
    
    private static final String SERVER_NAME = "json-editor";
    
    private static final String SERVER_VERSION = VersionUtil.getVersion();
    
    public static final int DEFAULT_PORT = 3000;
    
    private final ReadableModel model;
    
    private HttpServer server;
    
    private int port;
    
    private volatile boolean running;
    
    public JsonEditorMcpServer(final ReadableModel model)
    {
        if (model == null)
        {
            throw new IllegalArgumentException("model cannot be null");
        }
        this.model = model;
        this.running = false;
    }
    
    /**
     * Starts the MCP server on the specified port.
     *
     * @param port
     *         the port to listen on (localhost only, 1024-65535)
     *
     * @throws IOException
     *         if the server cannot be started
     * @throws IllegalArgumentException
     *         if port is invalid
     */
    public synchronized void start(final int port) throws IOException
    {
        if (port < 1024 || port > 65535)
        {
            throw new IllegalArgumentException("Port must be between 1024 and 65535");
        }
        
        if (running)
        {
            logger.warn("MCP Server already running on port {}, stopping first", this.port);
            stop();
        }
        
        this.port = port;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handleRequest);
        server.start();
        running = true;
        logger.info("MCP Server started on http://127.0.0.1:{}", port);
    }
    
    /**
     * Stops the MCP server gracefully.
     */
    public synchronized void stop()
    {
        if (server != null)
        {
            server.stop(0);
            server = null;
            running = false;
            logger.info("MCP Server stopped");
        }
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public int getPort()
    {
        return port;
    }
    
    private void handleRequest(final HttpExchange exchange) throws IOException
    {
        final String path = exchange.getRequestURI().getPath();
        
        if ("/health".equals(path))
        {
            handleHealthCheck(exchange);
        }
        else
        {
            handleMcpRequest(exchange);
        }
    }
    
    private void handleHealthCheck(final HttpExchange exchange) throws IOException
    {
        final String response = "{\"status\":\"ok\",\"service\":\"json-editor-mcp\"}";
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleMcpRequest(final HttpExchange exchange) throws IOException
    {
        if (!"POST".equals(exchange.getRequestMethod()))
        {
            sendJsonResponse(exchange, 405, createErrorResponse(null, -32600, "Method not allowed"));
            return;
        }
        
        String requestBody = null;
        try (InputStream is = exchange.getRequestBody())
        {
            requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            final JsonNode request = OBJECT_MAPPER.readTree(requestBody);
            
            final String method = request.path("method").asText();
            final JsonNode params = request.path("params");
            final JsonNode id = request.path("id");
            
            final String response = processMethod(method, params, id);
            sendJsonResponse(exchange, 200, response);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Invalid JSON in request: {}", requestBody, e);
            sendJsonResponse(exchange, 400, createErrorResponse(null, -32700, "Parse error"));
        }
        catch (Exception e)
        {
            logger.error("Error processing MCP request", e);
            sendJsonResponse(exchange, 500, createErrorResponse(null, -32603, "Internal error"));
        }
    }
    
    private String processMethod(final String method, final JsonNode params, final JsonNode id) throws JsonProcessingException
    {
        return switch (method)
        {
            case "initialize" -> handleInitialize(id);
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(params, id);
            default -> createErrorResponse(id, -32601, "Method not found: " + method);
        };
    }
    
    private String handleInitialize(final JsonNode id) throws JsonProcessingException
    {
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);
        
        final ObjectNode capabilities = OBJECT_MAPPER.createObjectNode();
        final ObjectNode tools = OBJECT_MAPPER.createObjectNode();
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        
        final ObjectNode serverInfo = OBJECT_MAPPER.createObjectNode();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);
        result.set("serverInfo", serverInfo);
        
        return createSuccessResponse(id, result);
    }
    
    private String handleToolsList(final JsonNode id) throws JsonProcessingException
    {
        final ArrayNode tools = OBJECT_MAPPER.createArrayNode();
        
        tools.add(createToolDefinition("get_current_file", "Get information about the currently open JSON file",
                OBJECT_MAPPER.createObjectNode()));
        
        tools.add(createToolDefinition("get_node", "Get a JSON node at a specific path",
                createSchemaWithProperty("path", "string", "JSON path (e.g., /root/child)")));
        
        tools.add(createToolDefinition("get_referenceable_objects", "List all referenceable object types defined in the schema",
                OBJECT_MAPPER.createObjectNode()));
        
        tools.add(createToolDefinition("get_referenceable_instances", "Get all instances of a referenceable object type",
                createSchemaWithProperty("referencing_key", "string", "The referencing key of the referenceable object type")));
        
        tools.add(createToolDefinition("get_examples", "Get example values for a JSON path",
                createSchemaWithProperty("path", "string", "JSON path to get examples for")));
        
        tools.add(createToolDefinition("get_schema_for_path", "Get the JSON schema definition for a specific path",
                createSchemaWithProperty("path", "string", "JSON path to get schema for")));
        
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.set("tools", tools);
        return createSuccessResponse(id, result);
    }
    
    private String handleToolsCall(final JsonNode params, final JsonNode id) throws JsonProcessingException
    {
        final String toolName = params.path("name").asText();
        final JsonNode arguments = params.path("arguments");
        
        return switch (toolName)
        {
            case "get_current_file" -> executeGetCurrentFile(id);
            case "get_node" -> executeGetNode(arguments, id);
            case "get_referenceable_objects" -> executeGetReferenceableObjects(id);
            case "get_referenceable_instances" -> executeGetReferenceableInstances(arguments, id);
            case "get_examples" -> executeGetExamples(arguments, id);
            case "get_schema_for_path" -> executeGetSchemaForPath(arguments, id);
            default -> createErrorResponse(id, -32602, "Unknown tool: " + toolName);
        };
    }
    
    private String executeGetCurrentFile(final JsonNode id) throws JsonProcessingException
    {
        final ObjectNode content = OBJECT_MAPPER.createObjectNode();
        
        if (model.getCurrentJSONFile() != null)
        {
            content.put("file_path", model.getCurrentJSONFile().getAbsolutePath());
            content.put("file_name", model.getCurrentJSONFile().getName());
        }
        else
        {
            content.putNull("file_path");
            content.putNull("file_name");
        }
        
        if (model.getCurrentSchemaFile() != null)
        {
            content.put("schema_path", model.getCurrentSchemaFile().getAbsolutePath());
        }
        else
        {
            content.putNull("schema_path");
        }
        
        content.put("has_content", model.getRootJson() != null);
        
        return createToolResult(id, content.toString());
    }
    
    private String executeGetNode(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        
        if (path.isEmpty())
        {
            return createToolResult(id, "Error: path parameter is required");
        }
        
        final JsonNodeWithPath node = model.getNodeForPath(path);
        if (node == null)
        {
            return createToolResult(id, "Error: No node found at path: " + path);
        }
        
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("path", node.getPath());
        result.put("display_name", node.getDisplayName());
        result.set("value", node.getNode());
        result.put("is_array", node.isArray());
        result.put("is_object", node.getNode().isObject());
        
        return createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
    }
    
    private String executeGetReferenceableObjects(final JsonNode id) throws JsonProcessingException
    {
        final List<ReferenceableObject> objects = model.getReferenceableObjects();
        final ArrayNode result = OBJECT_MAPPER.createArrayNode();
        
        for (final ReferenceableObject obj : objects)
        {
            final ObjectNode objNode = OBJECT_MAPPER.createObjectNode();
            objNode.put("path", obj.getPath());
            objNode.put("referencing_key", obj.getReferencingKey());
            objNode.put("key_property", obj.getKey());
            result.add(objNode);
        }
        
        return createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
    }
    
    private String executeGetReferenceableInstances(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String referencingKey = arguments.path("referencing_key").asText("");
        
        if (referencingKey.isEmpty())
        {
            return createToolResult(id, "Error: referencing_key parameter is required");
        }
        
        final ReferenceableObject refObject = model.getReferenceableObjectByReferencingKey(referencingKey);
        if (refObject == null)
        {
            return createToolResult(id, "Error: No referenceable object found with key: " + referencingKey);
        }
        
        final List<ReferenceableObjectInstance> instances = model.getReferenceableObjectInstances(refObject);
        final ArrayNode result = OBJECT_MAPPER.createArrayNode();
        
        for (final ReferenceableObjectInstance instance : instances)
        {
            final ObjectNode instNode = OBJECT_MAPPER.createObjectNode();
            instNode.put("path", instance.getPath());
            instNode.put("key", instance.getKey());
            instNode.put("display_name", instance.getFancyName());
            result.add(instNode);
        }
        
        return createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
    }
    
    private String executeGetExamples(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        
        if (path.isEmpty())
        {
            return createToolResult(id, "Error: path parameter is required");
        }
        
        final List<String> examples = model.getStringExamplesForPath(path);
        final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
        
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        
        final ArrayNode examplesArray = OBJECT_MAPPER.createArrayNode();
        examples.forEach(examplesArray::add);
        result.set("examples", examplesArray);
        
        final ArrayNode allowedArray = OBJECT_MAPPER.createArrayNode();
        allowedValues.forEach(allowedArray::add);
        result.set("allowed_values", allowedArray);
        
        return createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
    }
    
    private String executeGetSchemaForPath(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        
        if (path.isEmpty())
        {
            return createToolResult(id, "Error: path parameter is required");
        }
        
        final var schema = model.getSubschemaForPath(path);
        if (schema == null)
        {
            return createToolResult(id, "Error: No schema found for path: " + path);
        }
        
        return createToolResult(id, schema.getSchemaNode().toString());
    }
    
    private ObjectNode createToolDefinition(final String name, final String description, final ObjectNode inputSchema)
    {
        final ObjectNode tool = OBJECT_MAPPER.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        
        final ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", inputSchema);
        tool.set("inputSchema", schema);
        
        return tool;
    }
    
    private ObjectNode createSchemaWithProperty(final String propName, final String propType, final String description)
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        final ObjectNode prop = OBJECT_MAPPER.createObjectNode();
        prop.put("type", propType);
        prop.put("description", description);
        props.set(propName, prop);
        return props;
    }
    
    private String createSuccessResponse(final JsonNode id, final JsonNode result) throws JsonProcessingException
    {
        final ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return OBJECT_MAPPER.writeValueAsString(response);
    }
    
    private String createToolResult(final JsonNode id, final String content) throws JsonProcessingException
    {
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        final ArrayNode contentArray = OBJECT_MAPPER.createArrayNode();
        final ObjectNode textContent = OBJECT_MAPPER.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", content);
        contentArray.add(textContent);
        result.set("content", contentArray);
        
        return createSuccessResponse(id, result);
    }
    
    private String createErrorResponse(final JsonNode id, final int code, final String message)
    {
        try
        {
            final ObjectNode response = OBJECT_MAPPER.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.set("id", id);
            
            final ObjectNode error = OBJECT_MAPPER.createObjectNode();
            error.put("code", code);
            error.put("message", message);
            response.set("error", error);
            
            return OBJECT_MAPPER.writeValueAsString(response);
        }
        catch (JsonProcessingException e)
        {
            return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }
    
    private void sendJsonResponse(final HttpExchange exchange, final int statusCode, final String response) throws IOException
    {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        final byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(responseBytes);
        }
    }
}
