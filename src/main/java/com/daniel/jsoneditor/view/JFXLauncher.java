package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.AppService;
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
import java.awt.desktop.AppReopenedListener;
import java.io.File;
import java.util.List;


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

    @Override
    public void start(final Stage stage)
    {
        // Keep JavaFX runtime alive even without windows
        Platform.setImplicitExit(false);
        stage.close();

        // Core service starts MCP server immediately
        appService = new AppService();

        // Parse launch arguments
        final List<String> args = getParameters().getRaw();
        final boolean headless = args.contains("--headless");

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
            appService.createWindow();
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
                Desktop.getDesktop().addAppEventListener((AppReopenedListener) event ->
                        Platform.runLater(() ->
                        {
                            if (appService.getWindowCount() == 0)
                            {
                                logger.info("macOS reopen event — opening new window");
                                appService.createWindow();
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
        try
        {
            final PopupMenu menu = new PopupMenu();

            final MenuItem newWindowItem = new MenuItem("New Window");
            newWindowItem.addActionListener(evt -> Platform.runLater(() -> appService.createWindow()));
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
                    item.addActionListener(evt ->
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
