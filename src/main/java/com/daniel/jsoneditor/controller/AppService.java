package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


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
    
    private final List<AppWindow> windows = new CopyOnWriteArrayList<>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    
    public AppService()
    {
        this.fileSessionManager = new FileSessionManager();
        this.settingsController = new SettingsControllerImpl();
        this.mcpController = new McpController(fileSessionManager, settingsController);
    }
    
    /**
     * Creates a new editor window.
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
    
    /** Returns the shared file session manager. */
    public FileSessionManager getFileSessionManager()
    {
        return fileSessionManager;
    }
    
    /** Returns the shared settings controller. */
    public SettingsController getSettingsController()
    {
        return settingsController;
    }
    
    /** Returns the shared MCP controller. */
    public McpController getMcpController()
    {
        return mcpController;
    }
    
    /**
     * Shuts down all shared services. Called when the application exits.
     * Safe to call multiple times – only the first invocation performs work.
     */
    public void shutdown()
    {
        if (!shuttingDown.compareAndSet(false, true))
        {
            return;
        }
        logger.info("Shutting down AppService");
        mcpController.stopMcpServer();
    }
}
