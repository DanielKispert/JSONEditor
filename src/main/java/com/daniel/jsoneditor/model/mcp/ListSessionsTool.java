package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

// Extends ReadOnlyMcpTool for FileSessionManager injection and session_id schema helpers.
class ListSessionsTool extends ReadOnlyMcpTool
{
    public ListSessionsTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "list_sessions";
    }

    @Override
    public String getDescription()
    {
        return "List all active editing sessions. Returns session IDs and their associated file paths.";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final List<EditorSession> sessions = sessionManager.listSessions();
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        for (final EditorSession session : sessions)
        {
            final ObjectNode entry = JsonNodeFactory.instance.objectNode();
            entry.put("session_id", session.id());
            entry.put("json_path", session.jsonFile() != null ? session.jsonFile().getAbsolutePath() : "");
            entry.put("schema_path", session.schemaFile() != null ? session.schemaFile().getAbsolutePath() : "");
            entry.put("gui_owned", session.guiOwned());
            arr.add(entry);
        }
        return McpToolRegistry.createToolResult(id, arr);
    }
}
