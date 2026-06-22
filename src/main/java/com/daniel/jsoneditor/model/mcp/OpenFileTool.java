package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class OpenFileTool extends ReadOnlyMcpTool
{
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
        final ObjectNode props = JsonNodeFactory.instance.objectNode();

        final ObjectNode jsonPathProp = JsonNodeFactory.instance.objectNode();
        jsonPathProp.put("type", "string");
        jsonPathProp.put("description", "Absolute path to the JSON file");
        props.set("json_path", jsonPathProp);

        final ObjectNode schemaPathProp = JsonNodeFactory.instance.objectNode();
        schemaPathProp.put("type", "string");
        schemaPathProp.put("description", "Absolute path to the JSON schema file");
        props.set("schema_path", schemaPathProp);

        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        arr.add("json_path");
        arr.add("schema_path");
        return arr;
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String jsonPath = arguments.path("json_path").asText("");
        final String schemaPath = arguments.path("schema_path").asText("");

        final AttachResult attachResult = sessionManager.attachSession(jsonPath, schemaPath, false);
        if (!attachResult.success())
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, attachResult.error());
        }

        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("file_id", attachResult.sessionId());
        result.put("json_path", jsonPath);
        result.put("schema_path", schemaPath);

        return McpToolRegistry.createToolResult(id, result);
    }
}
