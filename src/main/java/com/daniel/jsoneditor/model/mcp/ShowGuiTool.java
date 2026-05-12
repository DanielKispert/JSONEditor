package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.controller.AppService;
import com.daniel.jsoneditor.controller.AppWindow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP tool that opens a new GUI window on demand.
 * Useful when the app runs in headless mode and an AI agent or user wants to see the editor.
 */
class ShowGuiTool extends McpTool
{
    private static final Logger logger = LoggerFactory.getLogger(ShowGuiTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AppService appService;

    public ShowGuiTool(final AppService appService)
    {
        this.appService = appService;
    }

    @Override
    public String getName()
    {
        return "show_gui";
    }

    @Override
    public String getDescription()
    {
        return "Opens a new JSON Editor GUI window. Use when running in headless mode to show the editor interface.";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        return OBJECT_MAPPER.createObjectNode();
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        if (appService.isShuttingDown())
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Cannot open window — application is shutting down");
        }

        Platform.runLater(() ->
        {
            final AppWindow window = appService.createWindow();
            if (window != null)
            {
                logger.info("GUI window opened via MCP tool");
            }
            else
            {
                logger.warn("GUI window creation failed — application may be shutting down");
            }
        });

        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("status", "queued");
        result.put("note", "Window creation requested. The window will appear shortly.");
        return McpToolRegistry.createToolResult(id, result);
    }
}
