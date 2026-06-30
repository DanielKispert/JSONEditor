package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.settings.Settings;
import java.io.File;

/**
 * Base class for read-only MCP tools. Resolves the target model from a session_id argument.
 */
public abstract class ReadOnlyMcpTool extends McpTool
{
    protected static final String SESSION_ID_REQUIRED_MESSAGE = "session_id argument is required";

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
     * Resolves the {@code session_id} argument to a {@link ReadableModel},
     * eliminating the double-lookup between validation and retrieval.
     * <p>Note: the returned model reference remains valid even if the session
     * is concurrently closed, but may represent stale state.</p>
     * <p>
     * Returns a {@link ResolveResult} where either {@link ResolveResult#model()} is non-null
     * (success) or {@link ResolveResult#error()} is non-null (failure). Tools should call
     * this at the start of {@code execute()} and return {@link ResolveResult#error()}
     * immediately when non-null.
     */
    protected ResolveResult resolveFileSession(final JsonNode arguments, final JsonNode id)
    {
        final String sessionId = getValidatedSessionId(arguments);
        if (sessionId == null)
        {
            return new ResolveResult(null, sessionIdRequiredError(id));
        }
        final EditorSession session = sessionManager.getSession(sessionId);
        if (session == null)
        {
            return new ResolveResult(null,
                    JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                            "Unknown session_id: " + sessionId));
        }
        return new ResolveResult(session.model(), null);
    }

    /**
     * Returns the session_id string from arguments if present and non-empty, or null if missing/empty.
     * Callers should return {@link #sessionIdRequiredError(JsonNode)} when this returns null.
     */
    protected String getValidatedSessionId(final JsonNode arguments)
    {
        final String sessionId = arguments.path("session_id").asText(null);
        if (sessionId == null || sessionId.isEmpty())
        {
            return null;
        }
        return sessionId;
    }

    /** Builds a JSON-RPC error response for a missing or empty session_id argument. */
    protected String sessionIdRequiredError(final JsonNode id)
    {
        return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, SESSION_ID_REQUIRED_MESSAGE);
    }

    protected static void addSessionIdProperty(final ObjectNode properties)
    {
        final ObjectNode sessionIdProp = JsonNodeFactory.instance.objectNode();
        sessionIdProp.put("type", "string");
        sessionIdProp.put("description", "Session ID of the file to operate on (from list_sessions or open_session)");
        properties.set("session_id", sessionIdProp);
    }

    protected static void addSessionIdRequired(final ArrayNode required)
    {
        required.add("session_id");
    }

    /**
     * Loads settings from the given file and applies them to the model.
     *
     * @return {@code true} if settings were loaded and applied; {@code false} if the file could not be parsed
     */
    protected static boolean applySettingsFile(final File settingsFile, final WritableModel writableModel)
    {
        final Settings settings = new JsonFileReaderAndWriterImpl().getJsonFromFile(settingsFile, Settings.class, true);
        if (settings == null)
        {
            return false;
        }
        writableModel.setSettings(settings);
        return true;
    }
}
