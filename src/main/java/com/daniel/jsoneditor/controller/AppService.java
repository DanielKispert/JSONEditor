package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Central application service that owns shared state across all editor windows.
 * Starts the MCP server immediately so external clients (e.g. OpenCode) can connect
 * regardless of whether any GUI windows are open.
 * Created once at app startup, lives until explicitly quit.
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
        this.mcpController = new McpController(fileSessionManager, settingsController, this);
        startMcpServer();
    }
    
    /**
     * Starts the MCP server if enabled in settings.
     * Called automatically during construction so the server is available before any window opens.
     */
    private void startMcpServer()
    {
        if (!settingsController.isMcpServerEnabled())
        {
            logger.info("MCP server disabled in settings, skipping auto-start");
            return;
        }
        mcpController.startMcpServer();
        if (mcpController.isMcpServerRunning())
        {
            logger.info("MCP server started on port {}", mcpController.getMcpServerPort());
        }
        else
        {
            logger.error("MCP server failed to start — check port availability");
        }
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
    
    /**
     * Called when a window is closed. Does NOT exit the application —
     * the service keeps running (MCP stays available) even without GUI windows.
     */
    private void onWindowClosed(final AppWindow window)
    {
        windows.remove(window);
        logger.info("Window closed. {} window(s) remaining. MCP server still running.", windows.size());
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
    
    /** Returns the number of currently open windows. */
    public int getWindowCount()
    {
        return windows.size();
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
