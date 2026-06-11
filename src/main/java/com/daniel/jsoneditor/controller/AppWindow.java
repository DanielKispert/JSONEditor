package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.impl.ControllerImpl;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Encapsulates a single app window with its own Model, Controller, View, and Stage.
 * Created by AppService, one per open file.
 */
public class AppWindow
{
    private static final Logger logger = LoggerFactory.getLogger(AppWindow.class);

    private final Stage stage;

    private final AppService appService;

    /** Mutable: null during the bootstrap picker phase, non-null once a file is loaded. */
    private Controller controller;

    /** Package-private constructor used by factory methods — no controller yet. */
    AppWindow(final AppService appService, final Stage stage)
    {
        this.appService = appService;
        this.stage = stage;
        this.controller = null;
    }

    /**
     * Creates a blank AppWindow with a new Stage, no controller. Package-private so that
     * {@link AppService} can also create blank windows for direct-load flows.
     *
     * @param appService the shared application service
     * @return a new blank AppWindow backed by a fresh Stage
     */
    static AppWindow createBlank(final AppService appService)
    {
        final Stage stage = new Stage();
        return new AppWindow(appService, stage);
    }

    /**
     * Bootstrap factory: creates an AppWindow that starts in Phase 1 (picker only, no model).
     * A {@link BootstrapController} handles the picker scene and transitions the window to Phase 2
     * (real editor) by calling {@link #attachLoadedController(ControllerImpl)} after the user
     * confirms a file selection.
     *
     * @param appService the shared application service
     * @return the new AppWindow (already showing the picker)
     */
    public static AppWindow bootstrap(final AppService appService)
    {
        final AppWindow window = createBlank(appService);
        final BootstrapController bootstrap = new BootstrapController(window.getStage(), appService, window);
        bootstrap.showPicker();
        return window;
    }

    /**
     * Replaces the (possibly null) bootstrap-phase controller with the real one after file load.
     * Called by {@link BootstrapController} once the model is ready.
     *
     * @param loadedController the fully initialised controller backed by the loaded model
     */
    public void attachLoadedController(final ControllerImpl loadedController)
    {
        this.controller = loadedController;
    }

    /**
     * Sets up close behavior: shuts down this window's controller.
     * Guards against a null controller (bootstrap window closed before a file was picked).
     *
     * @param onClose callback to run after this window closes (e.g. app exit check)
     */
    public void setOnClose(final Runnable onClose)
    {
        stage.setOnHiding(event ->
        {
            if (controller != null)
            {
                controller.shutdown();
            }
            // onClose (supplied by AppService) calls onWindowClosed(), which unregisters this window from
            // the WindowRegistry and checks whether the application should exit.
            if (onClose != null)
            {
                onClose.run();
            }
        });
    }

    /**
     * Returns the controller for this window's editor phase.
     *
     * @return the {@link Controller} once a file has been loaded, or {@code null} during the
     *         bootstrap (file-picker) phase before any file is selected. Callers must null-check.
     */
    public Controller getController()
    {
        return controller;
    }

    public Stage getStage()
    {
        return stage;
    }

    public boolean isShowing()
    {
        return stage.isShowing();
    }

    /**
     * Brings this window to the front, restoring it if iconified. If called off the JavaFX
     * Application Thread the request is scheduled to run there and an optimistic success is
     * returned.
     */
    public boolean focus()
    {
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(this::focus);
            return true; // optimistic — will be attempted on FX thread
        }
        if (!stage.isShowing())
        {
            logger.warn("Cannot focus a non-showing window");
            return false;
        }
        stage.setIconified(false);
        stage.toFront();
        stage.requestFocus();
        return true;
    }
}
