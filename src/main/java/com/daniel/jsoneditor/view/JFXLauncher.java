package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.AppService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.awt.desktop.AppReopenedListener;
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
            logger.info("Started in headless mode — MCP server running, no GUI window.");
        }
        else
        {
            appService.createWindow();
        }
        
        // macOS: reopen app when user clicks dock icon with no windows open
        registerMacOsReopenHandler();
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
            logger.debug("Could not register macOS reopen handler: {}", e.getMessage());
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
