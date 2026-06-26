package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.CloseFileResult;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Extends ReadOnlyMcpTool for FileSessionManager injection and session_id schema helpers.
class CloseSessionTool extends ReadOnlyMcpTool
{
    public CloseSessionTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "close_session";
    }

    @Override
    public String getDescription()
    {
        return "Close a headless editing session and release its resources. GUI sessions cannot be closed via MCP.";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = JsonNodeFactory.instance.objectNode();
        addSessionIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addSessionIdRequired(arr);
        return arr;
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String sessionId = arguments.path("session_id").asText(null);
        if (sessionId == null || sessionId.isEmpty())
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "session_id argument is required");
        }

        final CloseFileResult result = sessionManager.closeSession(sessionId);
        return switch (result)
        {
            case CLOSED ->
            {
                final ObjectNode payload = JsonNodeFactory.instance.objectNode();
                payload.put("ok", true);
                yield McpToolRegistry.createToolResult(id, payload);
            }
            case GUI_OWNED -> JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Session " + sessionId + " is owned by a GUI window and cannot be closed via MCP");
            case NOT_FOUND -> JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Unknown session_id: " + sessionId);
        };
    }
}
