package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GetNodeTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetNodeTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetNodeTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }
    
    @Override
    public String getName()
    {
        return "get_node";
    }
    
    @Override
    public String getDescription()
    {
        return "Get a JSON node at a specific path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = McpToolRegistry.createSchemaWithProperty("path", "string", "JSON path (e.g., /processes/0)");
        addFileIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        addFileIdRequired(arr);
        arr.add("path");
        return arr;
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String error = validateFileId(arguments, id);
        if (error != null)
        {
            return error;
        }
        final ReadableModel model = resolveModel(arguments);
        
        final String path = arguments.path("path").asText("");
        
        final JsonNodeWithPath node = model.getNodeForPath(path);
        if (node == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "No node found at path: " + path);
        }
        
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("path", node.getPath());
        result.put("display_name", node.getDisplayName());
        result.set("value", node.getNode());
        result.put("is_array", node.isArray());
        result.put("is_object", node.getNode().isObject());
        
        return McpToolRegistry.createToolResult(id, result);
    }
}
