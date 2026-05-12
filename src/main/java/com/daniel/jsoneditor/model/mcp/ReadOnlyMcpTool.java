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

    /**
     * Holds either a resolved {@link ReadableModel} on success, or a pre-built JSON-RPC
     * error response string on failure. Exactly one field is non-null.
     */
    record ResolveResult(ReadableModel model, String error) {}

    protected ReadOnlyMcpTool(final FileSessionManager sessionManager)
    {
        if (sessionManager == null)
        {
            throw new IllegalArgumentException("sessionManager cannot be null");
        }
        this.sessionManager = sessionManager;
    }

    /**
     * Atomically resolves the {@code file_id} argument to a {@link ReadableModel}.
     * <p>
     * Returns a {@link ResolveResult} where either {@link ResolveResult#model()} is non-null
     * (success) or {@link ResolveResult#error()} is non-null (failure). Tools should call
     * this at the start of {@code execute()} and return {@link ResolveResult#error()}
     * immediately when non-null, eliminating the TOCTOU window between validation and lookup.
     */
    protected ResolveResult resolveFileSession(final JsonNode arguments, final JsonNode id)
    {
        final String fileId = arguments.path("file_id").asText(null);
        if (fileId == null || fileId.isEmpty())
        {
            return new ResolveResult(null,
                    JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                            "file_id argument is required"));
        }
        final EditorSession session = sessionManager.getSession(fileId);
        if (session == null)
        {
            return new ResolveResult(null,
                    JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                            "Unknown file_id: " + fileId));
        }
        return new ResolveResult(session.model(), null);
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
