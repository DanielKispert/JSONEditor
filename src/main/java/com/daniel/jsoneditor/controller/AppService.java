package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Central application service that owns shared state across all editor windows.
 * Created once at app startup, lives until the app exits.
 */
public class AppService
{
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);
    
    private final FileSessionManager fileSessionManager;
    
    private final SettingsController settingsController;
    
    private final McpController mcpController;
    
    private final List<AppWindow> windows = new ArrayList<>();
    
    public AppService()
    {
        this.fileSessionManager = new FileSessionManager();
        this.settingsController = new SettingsControllerImpl();
        this.mcpController = new McpController(fileSessionManager, settingsController);
    }
    
    /**
     * Creates and shows a new editor window.
     *
     * @return the new window
     */
    public AppWindow createWindow()
    {
        final AppWindow window = new AppWindow(this);
        windows.add(window);
        window.setOnClose(() -> onWindowClosed(window));
        return window;
    }
    
    private void onWindowClosed(final AppWindow window)
    {
        windows.remove(window);
        if (windows.isEmpty())
        {
            shutdown();
            Platform.exit();
        }
    }
    
    public FileSessionManager getFileSessionManager()
    {
        return fileSessionManager;
    }
    
    public SettingsController getSettingsController()
    {
        return settingsController;
    }
    
    public McpController getMcpController()
    {
        return mcpController;
    }
    
    /**
     * Shuts down all shared services. Called when the application exits.
     */
    public void shutdown()
    {
        logger.info("Shutting down AppService");
        mcpController.stopMcpServer();
    }
}

