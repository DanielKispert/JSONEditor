package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.AppService;
import com.daniel.jsoneditor.controller.AppWindow;
import com.daniel.jsoneditor.controller.settings.RecentFilesManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Taskbar;
import java.awt.desktop.AppReopenedEvent;
import java.awt.desktop.AppReopenedListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * JavaFX entry point. Initializes the platform, creates the AppService (which starts
 * the MCP server immediately), and optionally opens a GUI window.
 * <p>
 * Supports {@code --headless} flag: starts the service without any GUI window.
 * The MCP server runs regardless; GUI windows can be opened on demand.
 */
public class JFXLauncher extends Application
{
    private static final Logger logger = LoggerFactory.getLogger(JFXLauncher.class);

    private AppService appService;

    public static void launchJFXApplication(final String[] args)
    {
        launch(args);
    }

    /**
     * Parses {@code --port N} from the launch arguments.
     * Returns the port number if valid, or {@code 0} if not specified (use settings default).
     */
    private int parsePortOverride(final List<String> args)
    {
        final int portIdx = args.indexOf("--port");
        if (portIdx >= 0 && portIdx + 1 < args.size())
        {
            try
            {
                final int port = Integer.parseInt(args.get(portIdx + 1));
                if (port > 0 && port <= 65535)
                {
                    return port;
                }
                logger.warn("Invalid --port value '{}': must be 1-65535, using settings default",
                        args.get(portIdx + 1));
            }
            catch (final NumberFormatException e)
            {
                logger.warn("Invalid --port argument '{}', using settings default", args.get(portIdx + 1));
            }
        }
        return 0;
    }

    @Override
    public void start(final Stage stage)
    {
        // Keep JavaFX runtime alive even without windows
        Platform.setImplicitExit(false);
        stage.close();

        // Parse launch arguments before creating AppService so port override can be applied
        final List<String> args = getParameters().getRaw();
        final boolean headless = args.contains("--headless");
        final int portOverride = parsePortOverride(args);

        // Core service starts MCP server immediately
        appService = new AppService(portOverride);

        if (headless)
        {
            if (appService.getMcpController().isMcpServerRunning())
            {
                logger.info("Started in headless mode — MCP server running on port {}, no GUI window.",
                    appService.getMcpController().getMcpServerPort());
            }
            else
            {
                logger.info("Started in headless mode — MCP server is disabled in settings, no GUI window.");
            }
        }
        else
        {
            final AppWindow window = appService.createWindow();
            if (window == null)
            {
                logger.error("Failed to create initial window — application is shutting down");
            }
        }

        // macOS: reopen app when user clicks dock icon with no windows open
        registerMacOsReopenHandler();

        // macOS: right-click dock menu with New Window + Recent Projects
        registerDockMenu();
    }

    /**
     * On macOS, clicking the dock icon when no windows are open triggers a "reopen" event.
     * We respond by opening a new editor window.
     */
    private void registerMacOsReopenHandler()
    {
        try
        {
            if (Desktop.isDesktopSupported())
            {
                Desktop.getDesktop().addAppEventListener((AppReopenedListener) (final AppReopenedEvent event) ->
                        Platform.runLater(() ->
                        {
                            if (appService.getWindowCount() == 0)
                            {
                                logger.info("macOS reopen event — opening new window");
                                final AppWindow window = appService.createWindow();
                                if (window == null)
                                {
                                    logger.warn("Could not create window on dock reopen — application is shutting down");
                                }
                            }
                        }));
            }
        }
        catch (Exception e)
        {
            // Not on macOS or AWT desktop not available — ignore silently
            logger.debug("Could not register macOS reopen handler", e);
        }
    }

    /**
     * Registers a right-click dock menu on macOS with "New Window" and "Recent Projects".
     * Silently skipped on platforms that do not support the Taskbar menu feature.
     */
    private void registerDockMenu()
    {
        try
        {
            if (!Taskbar.isTaskbarSupported())
            {
                return;
            }
            final Taskbar taskbar = Taskbar.getTaskbar();
            if (!taskbar.isSupported(Taskbar.Feature.MENU))
            {
                return;
            }
            rebuildDockMenu(taskbar);
            appService.getRecentFilesManager().addChangeListener(() -> rebuildDockMenu(taskbar));
        }
        catch (Exception e)
        {
            logger.debug("Could not register dock menu", e);
        }
    }

    /**
     * Builds and sets a fresh dock {@link PopupMenu} with the current recent files list.
     * Safe to call multiple times; each call replaces the previous menu.
     */
    private void rebuildDockMenu(final Taskbar taskbar)
    {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                final PopupMenu menu = new PopupMenu();

                final MenuItem newWindowItem = new MenuItem("New Window");
                newWindowItem.addActionListener((final ActionEvent evt) -> Platform.runLater(() ->
                {
                    final AppWindow window = appService.createWindow();
                    if (window == null)
                    {
                        logger.warn("Could not create window from dock menu — application is shutting down");
                    }
                }));
                menu.add(newWindowItem);

                final List<RecentFilesManager.RecentFile> recentFiles =
                        appService.getRecentFilesManager().getRecentFiles();
                if (!recentFiles.isEmpty())
                {
                    menu.addSeparator();
                    final Menu recentMenu = new Menu("Recent Projects");
                    for (final RecentFilesManager.RecentFile rf : recentFiles)
                    {
                        final File jsonFile = rf.jsonFile();
                        final File schemaFile = rf.schemaFile();
                        final MenuItem item = new MenuItem(jsonFile.getName());
                        item.addActionListener((final ActionEvent evt) ->
                                Platform.runLater(() -> appService.openFileInNewWindow(jsonFile, schemaFile)));
                        recentMenu.add(item);
                    }
                    menu.add(recentMenu);
                }

                taskbar.setMenu(menu);
            }
            catch (Exception e)
            {
                logger.debug("Could not rebuild dock menu", e);
            }
        });
    }

    @Override
    public void stop()
    {
        if (appService != null)
        {
            appService.shutdown();
        }
    }
}
