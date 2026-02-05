package com.daniel.jsoneditor.controller.mcp;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.mcp.JsonEditorMcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class McpController
{
    private static final Logger logger = LoggerFactory.getLogger(McpController.class);
    
    private final JsonEditorMcpServer mcpServer;
    
    private final SettingsController settingsController;
    
    public McpController(final ReadableModel model, final SettingsController settingsController)
    {
        this.mcpServer = new JsonEditorMcpServer(model);
        this.settingsController = settingsController;
    }
    
    /**
     * Starts the MCP server.
     * Logs errors but does not throw exceptions.
     */
    public void startMcpServer()
    {
        if (!mcpServer.isRunning())
        {
            try
            {
                mcpServer.start(settingsController.getMcpServerPort());
            }
            catch (IOException e)
            {
                logger.error("Failed to start MCP server on port {}: {}",
                    settingsController.getMcpServerPort(), e.getMessage());
            }
        }
    }
    
    /**
     * Stops the MCP server if it's running.
     */
    public void stopMcpServer()
    {
        mcpServer.stop();
    }
    
    /**
     * Checks if the MCP server is currently running.
     *
     * @return true if the server is running
     */
    public boolean isMcpServerRunning()
    {
        return mcpServer.isRunning();
    }
    
    /**
     * Gets the current port the server is running on or configured to use.
     *
     * @return the MCP server port
     */
    public int getMcpServerPort()
    {
        return mcpServer.isRunning() ? mcpServer.getPort() : settingsController.getMcpServerPort();
    }
}
