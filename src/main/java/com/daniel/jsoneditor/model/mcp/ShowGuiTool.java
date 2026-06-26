package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.controller.AppService;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.daniel.jsoneditor.util.CanonicalPaths;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * MCP tool that opens the JSON Editor GUI for a specific file.
 * Accepts either a session_id (existing session) or json_path + schema_path (new session).
 * Mutually exclusive: providing both is an error.
 */
class ShowGuiTool extends McpTool
{
    private static final Logger logger = LoggerFactory.getLogger(ShowGuiTool.class);

    private final AppService appService;
    private final FileSessionManager sessionManager;

    public ShowGuiTool(final AppService appService, final FileSessionManager sessionManager)
    {
        this.appService = appService;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getName()
    {
        return "show_gui";
    }

    @Override
    public String getDescription()
    {
        return "Open the JSON Editor GUI for a file. Provide EITHER session_id (attach to an existing session) "
                + "OR json_path + schema_path (open a new file directly). Mutually exclusive. "
                + "Returns session_id and gui_state: \"opened\" (new window) or \"focused\" (existing window brought to front).";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = JsonNodeFactory.instance.objectNode();

        final ObjectNode sessionIdProp = JsonNodeFactory.instance.objectNode();
        sessionIdProp.put("type", "string");
        sessionIdProp.put("description", "Session ID of an already-open session (from open_session or list_sessions). Mutually exclusive with json_path.");
        props.set("session_id", sessionIdProp);

        final ObjectNode jsonPathProp = JsonNodeFactory.instance.objectNode();
        jsonPathProp.put("type", "string");
        jsonPathProp.put("description", "Absolute path to the JSON file to open. Mutually exclusive with session_id.");
        props.set("json_path", jsonPathProp);

        final ObjectNode schemaPathProp = JsonNodeFactory.instance.objectNode();
        schemaPathProp.put("type", "string");
        schemaPathProp.put("description", "Absolute path to the JSON schema file. Required when json_path is provided.");
        props.set("schema_path", schemaPathProp);

        final ObjectNode settingsPathProp = JsonNodeFactory.instance.objectNode();
        settingsPathProp.put("type", "string");
        settingsPathProp.put("description", "Absolute path to an optional settings file (toolbar buttons, identifier mappings). Only used when json_path is provided.");
        props.set("settings_path", settingsPathProp);

        return props;
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        if (appService.isShuttingDown())
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Cannot open window — application is shutting down");
        }

        final String sessionId = arguments.path("session_id").asText(null);
        final String jsonPath = arguments.path("json_path").asText(null);
        final boolean hasSessionId = sessionId != null && !sessionId.isEmpty();
        final boolean hasJsonPath = jsonPath != null && !jsonPath.isEmpty();

        if (hasSessionId && hasJsonPath)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "session_id and json_path are mutually exclusive — provide one or the other, not both");
        }
        if (!hasSessionId && !hasJsonPath)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Either session_id or json_path must be provided");
        }

        final File jsonFile;
        final File schemaFile;
        final File settingsFile;
        final String resolvedSessionId;

        if (hasSessionId)
        {
            final EditorSession session = sessionManager.getSession(sessionId);
            if (session == null)
            {
                return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                        "Unknown session_id: " + sessionId);
            }
            jsonFile = session.jsonFile();
            schemaFile = session.schemaFile();
            settingsFile = null;
            resolvedSessionId = sessionId;
        }
        else
        {
            final String schemaPath = arguments.path("schema_path").asText(null);
            if (schemaPath == null || schemaPath.isEmpty())
            {
                return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                        "schema_path is required when json_path is provided");
            }
            jsonFile = new File(jsonPath);
            schemaFile = new File(schemaPath);
            final String settingsPath = arguments.path("settings_path").asText(null);
            settingsFile = (settingsPath != null && !settingsPath.isEmpty()) ? new File(settingsPath) : null;
            resolvedSessionId = null;
        }

        final String canonicalPath = CanonicalPaths.canonicalize(jsonFile);
        final boolean alreadyOpen = appService.getWindowRegistry().findByPath(canonicalPath).isPresent();
        final String guiState = alreadyOpen ? "focused" : "opened";

        final File finalSettingsFile = settingsFile;
        Platform.runLater(() ->
        {
            if (finalSettingsFile != null)
            {
                appService.openFileInNewWindowDirect(jsonFile, schemaFile, finalSettingsFile);
            }
            else
            {
                appService.openFileInNewWindow(jsonFile, schemaFile);
            }
        });

        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        if (resolvedSessionId != null)
        {
            result.put("session_id", resolvedSessionId);
        }
        result.put("gui_state", guiState);
        result.put("note", "Window operation queued on the JavaFX thread.");
        return McpToolRegistry.createToolResult(id, result);
    }
}
