package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.RecentFilesManager;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    private final RecentFilesManager recentFilesManager;

    private final SystemTrayManager systemTrayManager;

    private final List<AppWindow> windows = new CopyOnWriteArrayList<>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    /** Creates the service using the port configured in settings. */
    public AppService()
    {
        this(0);
    }

    /**
     * Creates the service, overriding the MCP server port when {@code portOverride > 0}.
     *
     * @param portOverride port to use for the MCP server, or {@code 0} to use the settings value
     */
    public AppService(final int portOverride)
    {
        this.fileSessionManager = new FileSessionManager();
        this.settingsController = new SettingsControllerImpl();
        this.recentFilesManager = new RecentFilesManager();
        this.mcpController = new McpController(fileSessionManager, settingsController, this);
        this.systemTrayManager = new SystemTrayManager(this);
        startMcpServer(portOverride);
    }

    /**
     * Starts the MCP server if enabled in settings.
     * Uses {@code portOverride} when positive; otherwise falls back to the settings port.
     * Called automatically during construction so the server is available before any window opens.
     * When the server starts successfully the system tray icon is shown.
     */
    private void startMcpServer(final int portOverride)
    {
        if (!settingsController.isMcpServerEnabled())
        {
            logger.info("MCP server disabled in settings, skipping auto-start");
            return;
        }
        final int port = portOverride > 0 ? portOverride : settingsController.getMcpServerPort();
        mcpController.startMcpServer(port);
        if (mcpController.isMcpServerRunning())
        {
            logger.info("MCP server started on port {}", mcpController.getMcpServerPort());
            systemTrayManager.show(mcpController.getMcpServerPort());
        }
        else
        {
            logger.error("MCP server failed to start — check port availability");
        }
    }

    /**
     * Creates a new editor window.
     *
     * @return the new {@link AppWindow}, or {@code null} if the application is shutting down
     */
    public AppWindow createWindow()
    {
        if (shuttingDown.get())
        {
            logger.info("Cannot create window — application is shutting down");
            return null;
        }
        final AppWindow window = new AppWindow(this);
        windows.add(window);
        window.setOnClose(() -> onWindowClosed(window));
        return window;
    }

    /**
     * Opens a new editor window and immediately loads the given JSON+schema file pair.
     * Must be called on the JavaFX Application Thread.
     */
    public void openFileInNewWindow(final File jsonFile, final File schemaFile)
    {
        final AppWindow window = createWindow();
        if (window == null)
        {
            return;
        }
        window.getController().jsonAndSchemaSelected(jsonFile, schemaFile, null);
    }

    /**
     * Called when a window is closed.
     * Exits the application when the last window closes and the MCP server is not running.
     * When MCP is enabled and running the service stays alive in the background.
     */
    private void onWindowClosed(final AppWindow window)
    {
        windows.remove(window);
        logger.info("Window closed. {} window(s) remaining.", windows.size());
        if (windows.isEmpty() && !mcpController.isMcpServerRunning())
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

    /** Returns the recent files manager. */
    public RecentFilesManager getRecentFilesManager()
    {
        return recentFilesManager;
    }

    /** Returns the number of currently open windows. */
    public int getWindowCount()
    {
        return windows.size();
    }

    /** Returns true if the application is shutting down. */
    public boolean isShuttingDown()
    {
        return shuttingDown.get();
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
        systemTrayManager.hide();
        mcpController.stopMcpServer();
    }
}
