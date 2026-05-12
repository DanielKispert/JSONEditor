package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.daniel.jsoneditor.model.sessions.OpenFileResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


class OpenFileTool extends ReadOnlyMcpTool
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public OpenFileTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }
    
    @Override
    public String getName()
    {
        return "open_file";
    }
    
    @Override
    public String getDescription()
    {
        return "Open a JSON file with its schema for reading. Returns a file_id to use with other tools.";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        
        final ObjectNode jsonPathProp = OBJECT_MAPPER.createObjectNode();
        jsonPathProp.put("type", "string");
        jsonPathProp.put("description", "Absolute path to the JSON file");
        props.set("json_path", jsonPathProp);
        
        final ObjectNode schemaPathProp = OBJECT_MAPPER.createObjectNode();
        schemaPathProp.put("type", "string");
        schemaPathProp.put("description", "Absolute path to the JSON schema file");
        props.set("schema_path", schemaPathProp);
        
        return props;
    }
    
    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        arr.add("json_path");
        arr.add("schema_path");
        return arr;
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String jsonPath = arguments.path("json_path").asText("");
        final String schemaPath = arguments.path("schema_path").asText("");
        
        final OpenFileResult openResult = sessionManager.openFile(jsonPath, schemaPath);
        if (!openResult.success())
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, openResult.error());
        }
        
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("file_id", openResult.sessionId());
        result.put("json_path", jsonPath);
        result.put("schema_path", schemaPath);
        
        return McpToolRegistry.createToolResult(id, result);
    }
}
