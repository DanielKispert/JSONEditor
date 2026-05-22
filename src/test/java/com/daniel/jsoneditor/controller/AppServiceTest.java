package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.model.sessions.AttachResult;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AppService#attachLoadedSession}.
 */
@ExtendWith(ApplicationExtension.class)
class AppServiceTest
{
    private static final String SIMPLE_JSON = "{\"name\":\"test\",\"value\":42}";

    private static final String SIMPLE_SCHEMA =
            "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
            + "\"type\":\"object\","
            + "\"properties\":{"
            + "\"name\":{\"type\":\"string\"},"
            + "\"value\":{\"type\":\"number\"}"
            + "}}";

    private AppService appService;
    private Stage fxStage;

    @Start
    void start(final Stage stage)
    {
        this.fxStage = stage;
    }

    @BeforeEach
    void setUp()
    {
        appService = new AppService(0);
    }

    @AfterEach
    void tearDown()
    {
        if (appService != null)
        {
            appService.shutdown();
        }
    }

    /**
     * Success path: valid JSON + schema files  AttachResult is successful, the session is
     * registered in FileSessionManager, and {@link AppWindow#attachLoadedController} is called
     * on the provided window. Must run on FX Application Thread because ControllerImpl sets a scene.
     */
    @Test
    void attachLoadedSession_success_returnsSuccess_andCallsWindowMethods(
            @TempDir final Path tempDir,
            final FxRobot robot) throws Exception
    {
        final Path jsonPath = tempDir.resolve("test.json");
        final Path schemaPath = tempDir.resolve("schema.json");
        Files.writeString(jsonPath, SIMPLE_JSON);
        Files.writeString(schemaPath, SIMPLE_SCHEMA);

        final AppWindow window = mock(AppWindow.class);
        final File jsonFile = jsonPath.toFile();
        final File schemaFile = schemaPath.toFile();

        // ControllerImpl construction sets a JavaFX scene  must run on the FX Application Thread
        final AttachResult[] holder = {null};
        robot.interact(() -> holder[0] = appService.attachLoadedSession(window, fxStage, jsonFile, schemaFile, null));
        final AttachResult result = holder[0];

        assertTrue(result.success(), "attachLoadedSession must succeed for valid files");
        assertNotNull(result.sessionId(), "sessionId must be non-null on success");
        verify(window).attachLoadedController(any());
        assertNotNull(appService.getFileSessionManager().getSession(result.sessionId()),
                "session must be registered in FileSessionManager after successful attach");
    }

    /**
     * Failure path: null schema  error returned immediately, no controller constructed, window untouched.
     */
    @Test
    void attachLoadedSession_nullSchema_returnsError()
    {
        final AppWindow window = mock(AppWindow.class);
        final File jsonFile = new File("/any/file.json");

        final AttachResult result = appService.attachLoadedSession(window, fxStage, jsonFile, null, null);

        assertFalse(result.success(), "null schema must return an error result");
        assertNotNull(result.error(), "error message must be non-null");
        assertTrue(result.error().toLowerCase().contains("schema"),
                "error must mention schema, got: " + result.error());
        verifyNoInteractions(window);
    }

    /**
     * Failure path: FileSessionManager rejects a second attach when a different schema is already
     * registered for the same canonical JSON path. The FSM error is returned verbatim; the second
     * window must not be mutated.
     */
    @Test
    void attachLoadedSession_schemaMismatch_returnsFsmError(
            @TempDir final Path tempDir,
            final FxRobot robot) throws Exception
    {
        final Path jsonPath = tempDir.resolve("test.json");
        final Path schemaAPath = tempDir.resolve("schemaA.json");
        final Path schemaBPath = tempDir.resolve("schemaB.json");
        Files.writeString(jsonPath, SIMPLE_JSON);
        Files.writeString(schemaAPath, SIMPLE_SCHEMA);
        Files.writeString(schemaBPath,
                "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{\"name\":{\"type\":\"string\"}}}");

        // First attach with schemaA  success path requires FX thread for ControllerImpl construction
        final AppWindow windowA = mock(AppWindow.class);
        final AttachResult[] firstHolder = {null};
        robot.interact(() -> firstHolder[0] =
                appService.attachLoadedSession(windowA, fxStage, jsonPath.toFile(), schemaAPath.toFile(), null));
        assertTrue(firstHolder[0].success(), "precondition: first attach with schemaA must succeed");

        // Second attach with schemaBPath for the same JSON  FSM rejects before ControllerImpl construction
        final AppWindow windowB = mock(AppWindow.class);
        final AttachResult resultB = appService.attachLoadedSession(
                windowB, fxStage, jsonPath.toFile(), schemaBPath.toFile(), null);

        assertFalse(resultB.success(), "schema mismatch must return an error result");
        assertNotNull(resultB.error(), "error must be non-null on schema mismatch");
        assertTrue(resultB.error().toLowerCase().contains("schema"),
                "FSM error must mention schema mismatch, got: " + resultB.error());
        verifyNoInteractions(windowB);
    }
}
