package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.impl.ControllerImpl;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.settings.Settings;
import com.daniel.jsoneditor.util.CanonicalPaths;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.settings.RecentFilesManager;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
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

    private final WindowRegistry windowRegistry;

    private final FileOpenCoordinator fileOpenCoordinator;

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
        this.windowRegistry = new WindowRegistry();
        this.fileOpenCoordinator = new FileOpenCoordinator(windowRegistry, this);
        startMcpServer(portOverride);
        this.systemTrayManager = new SystemTrayManager(this);
        try
        {
            if (mcpController.isMcpServerRunning())
            {
                systemTrayManager.show(mcpController.getMcpServerPort());
            }
        }
        catch (final Exception ex)
        {
            logger.warn("System tray icon could not be shown — app will continue without it", ex);
        }
    }

    // Starts MCP server if enabled; uses portOverride when > 0, else settings port.
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
        }
        else
        {
            logger.error("MCP server failed to start — check port availability");
        }
    }

    /**
     * Creates a new editor window.
     * Must be called on the JavaFX Application Thread.
     *
     * @return the new {@link AppWindow}, or {@code null} if the application is shutting down
     */
    public AppWindow createWindow()
    {
        assert Platform.isFxApplicationThread() : "Must be called on JavaFX Application Thread";
        if (shuttingDown.get())
        {
            logger.info("Cannot create window — application is shutting down");
            return null;
        }
        final AppWindow window = AppWindow.bootstrap(this);
        windows.add(window);
        window.setOnClose(() -> onWindowClosed(window));
        return window;
    }

    /**
     * Opens the given JSON+schema file pair in a window.
     * If a window is already showing this file, focuses it instead of creating a new one.
     * Must be called on the JavaFX Application Thread.
     */
    public void openFileInNewWindow(final File jsonFile, final File schemaFile)
    {
        fileOpenCoordinator.open(jsonFile, schemaFile);
    }

    /**
     * Opens a new editor window and immediately loads the given JSON+schema file pair.
     * Attaches a (possibly shared) session via
     * {@link com.daniel.jsoneditor.model.sessions.FileSessionManager#attachSession}.
     * Must be called on the JavaFX Application Thread.
     */
    public void openFileInNewWindowDirect(final File jsonFile, final File schemaFile)
    {
        openFileInNewWindowDirect(jsonFile, schemaFile, null);
    }

    /**
     * Opens a new editor window and immediately loads the given JSON+schema file pair with optional settings.
     * If a window for this file is already open, focuses it instead.
     * Must be called on the JavaFX Application Thread.
     */
    public void openFileInNewWindowDirect(final File jsonFile, final File schemaFile, final File settingsFile)
    {
        assert Platform.isFxApplicationThread() : "Must be called on JavaFX Application Thread";
        if (shuttingDown.get())
        {
            logger.info("Cannot open file — application is shutting down");
            return;
        }
        final AppWindow window = AppWindow.createBlank(this);
        windows.add(window);
        window.setOnClose(() -> onWindowClosed(window));
        try
        {
            final AttachResult result = attachLoadedSession(window, window.getStage(), jsonFile, schemaFile, settingsFile);
            if (!result.success())
            {
                windows.remove(window);
                window.getStage().close();
                final String error = result.error();
                Platform.runLater(() ->
                {
                    final Alert alert = new Alert(Alert.AlertType.ERROR, error != null ? error : "Failed to open file", ButtonType.OK);
                    alert.setTitle("Cannot open file");
                    alert.showAndWait();
                });
            }
        }
        catch (final RuntimeException e)
        {
            windows.remove(window);
            if (window.getStage().isShowing())
            {
                window.getStage().close();
            }
            logger.error("Unexpected error while opening file — window cleaned up", e);
            final String message = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while opening the file";
            Platform.runLater(() ->
            {
                final Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
                alert.setTitle("Cannot open file");
                alert.showAndWait();
            });
        }
    }

    // Exits when last window closes, unless MCP server is running.
    private void onWindowClosed(final AppWindow window)
    {
        windowRegistry.unregisterWindow(window);
        windows.remove(window);
        logger.info("Window closed. {} window(s) remaining.", windows.size());
        if (windows.isEmpty() && !mcpController.isMcpServerRunning())
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

    public RecentFilesManager getRecentFilesManager()
    {
        return recentFilesManager;
    }

    public WindowRegistry getWindowRegistry()
    {
        return windowRegistry;
    }

    public FileOpenCoordinator getFileOpenCoordinator()
    {
        return fileOpenCoordinator;
    }

    /**
     * Attaches a loaded file session and wires a new {@link ControllerImpl} to the given window/stage.
     * Handles: FSM session attach, optional settings application, controller construction,
     * window registration. Package-private — called by {@link AppWindow} factory methods and
     * by {@link #openFileInNewWindowDirect}.
     *
     * @param window       the AppWindow that will host the editor
     * @param stage        the JavaFX stage for the window
     * @param jsonFile     the JSON file to open
     * @param schemaFile   the schema file; must not be {@code null} — an {@link AttachResult#ofError} is returned if null
     * @param settingsFile optional settings file, may be {@code null}
     * @return the FSM {@link AttachResult}; on failure {@link AttachResult#success()} is {@code false}
     */
    AttachResult attachLoadedSession(final AppWindow window, final Stage stage, final File jsonFile,
            final File schemaFile, final File settingsFile)
    {
        if (jsonFile == null)
        {
            return AttachResult.ofError("JSON file is required");
        }
        if (schemaFile == null)
        {
            return AttachResult.ofError("Schema file is required and must not be null");
        }

        final AttachResult result = fileSessionManager.attachSession(
                jsonFile.getAbsolutePath(),
                schemaFile.getAbsolutePath(),
                true);
        if (!result.success())
        {
            logger.warn("attachSession failed for {}: {}", jsonFile, result.error());
            return result;
        }

        try
        {
            final EditorSession session = fileSessionManager.getSession(result.sessionId());
            if (session == null)
            {
                fileSessionManager.detachSession(result.sessionId());
                return AttachResult.ofError("Session unavailable after attach");
            }
            final ReadableModel sessionModel = session.model();
            if (!(sessionModel instanceof WritableModel writableModel))
            {
                throw new IllegalStateException("Session model does not implement WritableModel: " + sessionModel.getClass());
            }

            if (settingsFile != null && !settingsFile.getPath().isEmpty() && settingsFile.exists())
            {
                final Settings settings = new JsonFileReaderAndWriterImpl().getJsonFromFile(settingsFile, Settings.class, true);
                if (settings != null)
                {
                    writableModel.setSettings(settings);
                }
            }

            final ControllerImpl controller = new ControllerImpl(writableModel, sessionModel, stage, this, jsonFile, schemaFile,
                    result.sessionId());
            controller.setAppWindow(window);
            controller.registerInWindowRegistry(CanonicalPaths.canonicalize(jsonFile));
            window.attachLoadedController(controller);
            return result;
        }
        catch (final RuntimeException e)
        {
            try
            {
                fileSessionManager.detachSession(result.sessionId());
            }
            catch (final Exception detachEx)
            {
                logger.warn("detachSession failed during cleanup after exception; session {} may leak", result.sessionId(), detachEx);
            }
            throw e;
        }
    }

    public int getWindowCount()
    {
        return windows.size();
    }

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
        fileSessionManager.closeAllHeadlessSessions();
        mcpController.stopMcpServer();
        // Close all GUI windows to trigger their onHiding cleanup (controller.shutdown() + session detach)
        final Runnable closeWindows = () ->
        {
            for (final AppWindow window : new ArrayList<>(windows))
            {
                if (window.isShowing())
                {
                    window.getStage().close();
                }
            }
        };
        if (Platform.isFxApplicationThread())
        {
            closeWindows.run();
        }
        else
        {
            Platform.runLater(closeWindows);
        }
    }
}
