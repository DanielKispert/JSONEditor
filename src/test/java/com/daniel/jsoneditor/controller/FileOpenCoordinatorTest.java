package com.daniel.jsoneditor.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(ApplicationExtension.class)
class FileOpenCoordinatorTest
{
    @TempDir
    Path tempDir;

    @Test
    void open_pathAlreadyInRegistry_focusesExistingWindowWithoutOpeningNew(final FxRobot robot) throws Exception
    {
        final File jsonFile = tempDir.resolve("test.json").toFile();
        final File schemaFile = tempDir.resolve("schema.json").toFile();
        Files.writeString(jsonFile.toPath(), "{\"a\":1}");
        Files.writeString(schemaFile.toPath(), "{}");

        final String canonicalPath = jsonFile.getCanonicalPath();
        final WindowRegistry registry = new WindowRegistry();
        final AppWindow mockWindow = mock(AppWindow.class);
        when(mockWindow.isShowing()).thenReturn(true);
        final AppService mockAppService = mock(AppService.class);
        final FileOpenCoordinator coordinator = new FileOpenCoordinator(registry, mockAppService);

        robot.interact(() ->
        {
            registry.register(canonicalPath, mockWindow);
            coordinator.open(jsonFile, schemaFile);
        });

        verify(mockWindow, times(1)).focus();
        verifyNoInteractions(mockAppService);
    }

    @Test
    void open_pathNotInRegistry_opensNewWindowViaAppService(final FxRobot robot) throws Exception
    {
        final File jsonFile = tempDir.resolve("fresh.json").toFile();
        final File schemaFile = tempDir.resolve("schema.json").toFile();
        Files.writeString(jsonFile.toPath(), "{\"a\":1}");
        Files.writeString(schemaFile.toPath(), "{}");

        final WindowRegistry registry = new WindowRegistry();
        final AppService mockAppService = mock(AppService.class);
        final FileOpenCoordinator coordinator = new FileOpenCoordinator(registry, mockAppService);

        robot.interact(() -> coordinator.open(jsonFile, schemaFile));

        verify(mockAppService, times(1)).openFileInNewWindowDirect(jsonFile, schemaFile);
    }
}
