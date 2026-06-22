package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ListFilesTool extends ReadOnlyMcpTool
{
    public ListFilesTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "list_files";
    }

    @Override
    public String getDescription()
    {
        return "List all currently open file sessions with their IDs and paths";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final ArrayNode result = JsonNodeFactory.instance.arrayNode();

        for (final EditorSession session : sessionManager.listSessions())
        {
            final ObjectNode entry = JsonNodeFactory.instance.objectNode();
            entry.put("file_id", session.id());
            entry.put("json_path", session.jsonFile() != null ? session.jsonFile().getAbsolutePath() : null);
            entry.put("schema_path", session.schemaFile() != null ? session.schemaFile().getAbsolutePath() : null);
            entry.put("gui_owned", session.guiOwned());
            result.add(entry);
        }

        return McpToolRegistry.createToolResult(id, result);
    }
}

