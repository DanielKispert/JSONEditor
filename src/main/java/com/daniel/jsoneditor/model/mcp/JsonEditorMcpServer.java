package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.util.VersionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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


/**
 * MCP (Model Context Protocol) Server for the JSON Editor.
 * Provides read-only and (optionally) write access to the current editor session via HTTP JSON-RPC.
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
    
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;
    private static final int HTTP_INTERNAL_ERROR = 500;
    
    private static final int JSONRPC_PARSE_ERROR = -32700;
    private static final int JSONRPC_INVALID_REQUEST = -32600;
    private static final int JSONRPC_METHOD_NOT_FOUND = -32601;
    private static final int JSONRPC_INVALID_PARAMS = -32602;
    private static final int JSONRPC_INTERNAL_ERROR = -32603;
    
    private final McpToolRegistry toolRegistry;
    
    private HttpServer server;
    
    private int port;
    
    private volatile boolean running;
    
    /**
     * Creates MCP server with both readable and writable model.
     * WritableModel is passed to registry for future write-tool support.
     * Currently only read-only tools are enabled in registry.
     *
     * @param writableModel for read and write operations (passed to tools when enabled)
     */
    public JsonEditorMcpServer(final WritableModel writableModel)
    {
        if (writableModel == null)
        {
            throw new IllegalArgumentException("writableModel cannot be null");
        }
        this.toolRegistry = new McpToolRegistry(writableModel);
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
        sendJsonResponse(exchange, HTTP_OK, response);
    }
    
    private void handleMcpRequest(final HttpExchange exchange) throws IOException
    {
        if (!"POST".equals(exchange.getRequestMethod()))
        {
            sendJsonResponse(exchange, HTTP_METHOD_NOT_ALLOWED, createErrorResponse(null, JSONRPC_INVALID_REQUEST, "Method not allowed"));
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
            sendJsonResponse(exchange, HTTP_OK, response);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Invalid JSON in request: {}", requestBody, e);
            sendJsonResponse(exchange, HTTP_BAD_REQUEST, createErrorResponse(null, JSONRPC_PARSE_ERROR, "Parse error"));
        }
    }
    
    private String processMethod(final String method, final JsonNode params, final JsonNode id) throws JsonProcessingException
    {
        return switch (method)
        {
            case "initialize" -> handleInitialize(id);
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(params, id);
            default -> createErrorResponse(id, JSONRPC_METHOD_NOT_FOUND, "Method not found: " + method);
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
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.set("tools", toolRegistry.getToolDefinitions());
        return createSuccessResponse(id, result);
    }
    
    private String handleToolsCall(final JsonNode params, final JsonNode id) throws JsonProcessingException
    {
        final String toolName = params.path("name").asText();
        final JsonNode arguments = params.path("arguments");
        
        logger.info("MCP tool called: {} with arguments: {}", toolName, arguments);
        
        final McpTool tool = toolRegistry.getTool(toolName);
        if (tool == null)
        {
            logger.warn("Unknown tool requested: {}", toolName);
            return createErrorResponse(id, JSONRPC_INVALID_PARAMS, "Unknown tool: " + toolName);
        }

        // Validate arguments against complete input schema (including required properties)
        final ObjectNode inputSchema = McpToolRegistry.buildInputSchema(tool);
        try
        {
            McpArgumentValidator.validate(inputSchema, arguments);
        }
        catch (ValidationException e)
        {
            logger.warn("Tool {} validation failed: {}", toolName, e.getMessage());
            return createErrorResponse(id, JSONRPC_INVALID_PARAMS, e.getMessage());
        }
        
        final String result = tool.execute(arguments, id);
        logger.debug("Tool {} completed successfully", toolName);
        return result;
    }

    private String createSuccessResponse(final JsonNode id, final JsonNode result) throws JsonProcessingException
    {
        final ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return OBJECT_MAPPER.writeValueAsString(response);
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

    public static String createErrorResponseStatic(final JsonNode id, final int code, final String message)
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
