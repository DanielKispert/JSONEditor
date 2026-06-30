package com.daniel.jsoneditor.model.mcp;


import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Extends ReadOnlyMcpTool for FileSessionManager injection and session_id schema helpers.
class OpenSessionTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(OpenSessionTool.class);

    public OpenSessionTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "open_session";
    }

    @Override
    public String getDescription()
    {
        return "Open a JSON file as an editing session. Returns a session_id for use with all other tools. Optionally provide a schema and settings file.";
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

        final ObjectNode settingsPathProp = JsonNodeFactory.instance.objectNode();
        settingsPathProp.put("type", "string");
        settingsPathProp.put("description", "Absolute path to an optional settings JSON file (toolbar buttons, identifier mappings)");
        props.set("settings_path", settingsPathProp);

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

        final String settingsPath = arguments.path("settings_path").asText(null);
        if (settingsPath != null && !settingsPath.isEmpty())
        {
            final File settingsFile = new File(settingsPath);
            final EditorSession session = sessionManager.getSession(attachResult.sessionId());
            if (!settingsFile.exists())
            {
                logger.warn("Settings file not found, session will use default settings: {}", settingsPath);
            }
            else if (session != null && session.model() instanceof WritableModel writableModel)
            {
                if (!applySettingsFile(settingsFile, writableModel))
                {
                    logger.warn("Settings file could not be parsed, session will use default settings: {}", settingsPath);
                }
            }
        }

        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("session_id", attachResult.sessionId());
        result.put("json_path", jsonPath);
        result.put("schema_path", schemaPath);

        return McpToolRegistry.createToolResult(id, result);
    }
}
