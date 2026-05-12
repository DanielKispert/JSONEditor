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
    protected static final String FILE_ID_REQUIRED_MESSAGE = "file_id argument is required";

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
        final String fileId = getValidatedFileId(arguments);
        if (fileId == null)
        {
            return new ResolveResult(null, fileIdRequiredError(id));
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

    /**
     * Returns the file_id string from arguments if present and non-empty, or null if missing/empty.
     * Callers should return {@link #fileIdRequiredError(JsonNode)} when this returns null.
     */
    protected String getValidatedFileId(final JsonNode arguments)
    {
        final String fileId = arguments.path("file_id").asText(null);
        if (fileId == null || fileId.isEmpty())
        {
            return null;
        }
        return fileId;
    }

    /** Builds a JSON-RPC error response for a missing or empty file_id argument. */
    protected String fileIdRequiredError(final JsonNode id)
    {
        return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, FILE_ID_REQUIRED_MESSAGE);
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
