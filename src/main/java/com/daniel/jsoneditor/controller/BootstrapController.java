package com.daniel.jsoneditor.controller;

import java.io.File;
import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelectionScene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns Phase 1 of the two-phase bootstrap: shows the file picker, no model yet.
 * On file pick, attaches a session via {@link FileSessionManager}, then transitions
 * the owning {@link AppWindow} to a real {@link ControllerImpl} via
 * {@link AppWindow#attachLoadedController(ControllerImpl)}.
 */
public final class BootstrapController
{
    private static final Logger logger = LoggerFactory.getLogger(BootstrapController.class);

    private final Stage stage;
    private final AppService appService;
    private final AppWindow appWindow;

    public BootstrapController(final Stage stage, final AppService appService, final AppWindow appWindow)
    {
        this.stage = stage;
        this.appService = appService;
        this.appWindow = appWindow;
    }

    /**
     * Shows the file picker scene.
     * <p>
     * A disposable empty {@link ModelImpl} is passed as a placeholder — {@link JSONSelectionScene}
     * requires a non-null {@link com.daniel.jsoneditor.model.ReadableModel} for its parent
     * constructor but never reads from it during the picker phase. The UIHandler argument is
     * {@code null} for the same reason: {@code JSONSelectionScene.getScene()} never invokes it.
     * When the user confirms a selection, {@link #onFilesPicked(File, File, File)} is called.
     */
    public void showPicker()
    {
        // JSONSelectionScene currently requires a non-null ReadableModel even though the picker
        // phase has no file/data yet. We pass an empty placeholder ModelImpl that's never read
        // from and becomes garbage-collectable once the picker is dismissed. A future cleanup
        // would refactor JSONSelectionScene to not require a model at all.
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

    private void onFilesPicked(final File jsonFile, final File schemaFile, final File settingsFile)
    {
        final AttachResult result = appService.attachLoadedSession(appWindow, stage, jsonFile, schemaFile, settingsFile);
        if (!result.success())
        {
            logger.warn("attachSession failed: {}", result.error());
            // Picker stays visible — the user can correct the file selection
        }
    }
}
