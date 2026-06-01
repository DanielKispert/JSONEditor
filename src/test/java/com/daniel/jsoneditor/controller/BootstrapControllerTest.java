package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.impl.ControllerImpl;
import com.daniel.jsoneditor.model.sessions.AttachResult;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BootstrapController} — the Phase 1 bootstrap controller
 * that manages the file-picker scene before a session is attached.
 */
class BootstrapControllerTest
{
    /**
     * Exercises both the success and failure branches of {@link BootstrapController#onFilesPicked}.
     * Success: attachLoadedSession called with correct args; no alert scheduled; stage not closed.
     * Failure: onFilesPicked does not throw; stage stays visible; error alert scheduled via Platform.runLater.
     * ISE thrown: onFilesPicked does not propagate; stage stays open.
     * Fresh controller instances are used per branch to avoid state accumulation.
     */
    @Test
    void onFilesPicked_handlesAllOutcomes()
    {
        final File jsonFile = new File("/data/file.json");
        final File schemaFile = new File("/data/schema.json");
        final File settingsFile = new File("/data/settings.json");

        // === Success path: attachLoadedSession called with correct args; no alert, stage not closed ===
        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final Stage stage = mock(Stage.class);
            final AppService appService = mock(AppService.class);
            final AppWindow appWindow = mock(AppWindow.class);
            when(appService.attachLoadedSession(appWindow, stage, jsonFile, schemaFile, settingsFile))
                    .thenReturn(AttachResult.ofSuccess("gui-abc123"));

            final BootstrapController bootstrap = new BootstrapController(stage, appService, appWindow);
            bootstrap.onFilesPicked(jsonFile, schemaFile, settingsFile);

            verify(appService).attachLoadedSession(appWindow, stage, jsonFile, schemaFile, settingsFile);
            // On success: no error alert scheduled via Platform.runLater, picker stage not closed
            platformMock.verify(() -> Platform.runLater(any(Runnable.class)), never());
            verify(stage, never()).close();
            verify(stage, never()).hide();
        }

        // === Failure path: stage stays open (picker visible), error alert scheduled via Platform.runLater ===
        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final Stage stage = mock(Stage.class);
            final AppService appService = mock(AppService.class);
            final AppWindow appWindow = mock(AppWindow.class);
            when(appService.attachLoadedSession(any(), any(), any(), any(), any()))
                    .thenReturn(AttachResult.ofError("JSON file does not exist: /bad.json"));

            final BootstrapController bootstrap = new BootstrapController(stage, appService, appWindow);
            assertDoesNotThrow(
                    () -> bootstrap.onFilesPicked(new File("/bad.json"), new File("/schema.json"), null),
                    "onFilesPicked must not throw on attach failure");

            // Picker stays: stage is NOT closed or hidden
            verify(stage, never()).close();
            verify(stage, never()).hide();

            // Error alert was scheduled via Platform.runLater — not executed synchronously (no FX toolkit needed)
            platformMock.verify(() -> Platform.runLater(any(Runnable.class)));
        }

        // === Branch 3: attachLoadedSession throws ISE → no propagation, stage stays open ===
        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final Stage stage = mock(Stage.class);
            final AppService appService = mock(AppService.class);
            final AppWindow appWindow = mock(AppWindow.class);
            when(appService.attachLoadedSession(any(), any(), any(), any(), any()))
                    .thenThrow(new IllegalStateException("session detached: duplicate attach"));

            final BootstrapController bootstrap = new BootstrapController(stage, appService, appWindow);
            assertDoesNotThrow(
                    () -> bootstrap.onFilesPicked(new File("/data/file.json"), new File("/data/schema.json"), null),
                    "onFilesPicked must not propagate IllegalStateException from attachLoadedSession");

            verify(stage, never()).close();
            verify(stage, never()).hide();
        }
    }

    /**
     * Two-phase init: AppWindow starts with a null controller (picker phase),
     * and transitions to a real controller after attachLoadedController is called.
     */
    @Test
    void appWindow_twoPhase_init_controllerTransition()
    {
        final AppService appService = mock(AppService.class);
        final Stage stage = mock(Stage.class);
        final AppWindow window = new AppWindow(appService, stage);

        assertNull(window.getController(), "controller must be null in Phase 1 (picker phase)");

        final ControllerImpl loadedController = mock(ControllerImpl.class);
        window.attachLoadedController(loadedController);

        assertSame(loadedController, window.getController(),
                "controller must be the real one after attachLoadedController (Phase 2)");
    }

    
}
