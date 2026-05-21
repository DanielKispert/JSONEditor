package com.daniel.jsoneditor.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class FileOpenCoordinatorTest
{
    @TempDir
    Path tempDir;

    /**
     * When a window is already showing the requested file, open() must focus that window
     * and must NOT call AppService — the deduplication guard.
     */
    @Test
    void open_pathAlreadyInRegistry_focusesExistingWindowWithoutOpeningNew() throws Exception
    {
        final File jsonFile = tempDir.resolve("test.json").toFile();
        final File schemaFile = tempDir.resolve("schema.json").toFile();
        Files.writeString(jsonFile.toPath(), "{\"a\":1}");
        Files.writeString(schemaFile.toPath(), "{}");

        // Register a mock window at the canonical path that FileOpenCoordinator will compute
        final String canonicalPath = jsonFile.getCanonicalPath();
        final WindowRegistry registry = new WindowRegistry();
        final AppWindow mockWindow = mock(AppWindow.class);
        registry.register(canonicalPath, mockWindow);

        final AppService mockAppService = mock(AppService.class);
        final FileOpenCoordinator coordinator = new FileOpenCoordinator(registry, mockAppService);

        coordinator.open(jsonFile, schemaFile);

        // Existing window must be focused; AppService must not be touched
        verify(mockWindow, times(1)).focus();
        verifyNoInteractions(mockAppService);
    }

    /**
     * When no window is currently showing the requested file, open() must delegate
     * to AppService.openFileInNewWindowDirect to create a new window.
     */
    @Test
    void open_pathNotInRegistry_opensNewWindowViaAppService() throws Exception
    {
        final File jsonFile = tempDir.resolve("fresh.json").toFile();
        final File schemaFile = tempDir.resolve("schema.json").toFile();
        Files.writeString(jsonFile.toPath(), "{\"a\":1}");
        Files.writeString(schemaFile.toPath(), "{}");

        final WindowRegistry registry = new WindowRegistry(); // empty — no window registered
        final AppService mockAppService = mock(AppService.class);
        final FileOpenCoordinator coordinator = new FileOpenCoordinator(registry, mockAppService);

        coordinator.open(jsonFile, schemaFile);

        // A new window must be requested via AppService
        verify(mockAppService, times(1)).openFileInNewWindowDirect(jsonFile, schemaFile);
    }
}
