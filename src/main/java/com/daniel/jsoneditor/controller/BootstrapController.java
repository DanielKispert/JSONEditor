package com.daniel.jsoneditor.controller;

import java.io.File;
import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelectionScene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns Phase 1 of the two-phase bootstrap: shows the file picker, no model yet.
 * On file pick, attaches a session via {@link FileSessionManager}, then transitions
 * the owning {@link AppWindow} to a real {@link ControllerImpl} via
 * {@link AppWindow#attachLoadedController(ControllerImpl)}.
 * <p>Must be called on the JavaFX Application Thread.</p>
 */
public final class BootstrapController
{
    private static final Logger logger = LoggerFactory.getLogger(BootstrapController.class);

    private final Stage stage;
    private final AppService appService;
    private final AppWindow appWindow;

    /** Owns the Phase-1 (picker) lifecycle for a new {@link AppWindow}. */
    public BootstrapController(final Stage stage, final AppService appService, final AppWindow appWindow)
    {
        this.stage = stage;
        this.appService = appService;
        this.appWindow = appWindow;
    }

    /**
     * Shows the file picker scene.
     * Must be called on the JavaFX Application Thread.
     * <p>
     * A disposable empty {@link ModelImpl} is passed as a placeholder — {@link JSONSelectionScene}
     * requires a non-null {@link com.daniel.jsoneditor.model.ReadableModel} for its parent
     * constructor but never reads from it during the picker phase. The UIHandler argument is
     * {@code null} for the same reason: {@code JSONSelectionScene.getScene()} never invokes it.
     * When the user confirms a selection, {@link #onFilesPicked(File, File, File)} is called.
     */
    public void showPicker()
    {
        final ModelImpl placeholder = ModelFactory.createEmpty();
        final JSONSelectionScene pickerScene = new JSONSelectionScene(
                null,
                appService.getSettingsController(),
                placeholder,
                this::onFilesPicked);
        stage.setScene(pickerScene.getScene(stage));
        stage.setWidth(700);
        stage.setHeight(300);
        stage.show();
    }

    void onFilesPicked(final File jsonFile, final File schemaFile, final File settingsFile)
    {
        final AttachResult result = appService.attachLoadedSession(appWindow, stage, jsonFile, schemaFile, settingsFile);
        if (!result.success())
        {
            logger.warn("attachSession failed: {}", result.error());
            final String message = result.error() != null ? result.error() : "Failed to open file";
            Platform.runLater(() ->
            {
                final Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
                alert.setTitle("Cannot open file");
                alert.showAndWait();
            });
            // Picker stays visible — the user can correct the file selection
        }
    }
}
