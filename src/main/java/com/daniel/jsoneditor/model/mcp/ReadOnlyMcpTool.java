package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Base class for read-only MCP tools. Resolves the target model from a file_id argument.
 */
public abstract class ReadOnlyMcpTool extends McpTool
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    protected final FileSessionManager sessionManager;
    
    protected ReadOnlyMcpTool(final FileSessionManager sessionManager)
    {
        if (sessionManager == null)
        {
            throw new IllegalArgumentException("sessionManager cannot be null");
        }
        this.sessionManager = sessionManager;
    }
    
    protected ReadableModel resolveModel(final JsonNode arguments)
    {
        final String fileId = arguments.path("file_id").asText(null);
        if (fileId == null)
        {
            return null;
        }
        final EditorSession session = sessionManager.getSession(fileId);
        return session != null ? session.model() : null;
    }
    
    protected static void addFileIdProperty(final ObjectNode properties)
    {
        final ObjectNode fileIdProp = OBJECT_MAPPER.createObjectNode();
        fileIdProp.put("type", "string");
        fileIdProp.put("description", "Session ID of the file to operate on (from list_files or open_file)");
        properties.set("file_id", fileIdProp);
    }
    
    protected static void addFileIdRequired(final ArrayNode required)
    {
        required.add("file_id");
    }
}
