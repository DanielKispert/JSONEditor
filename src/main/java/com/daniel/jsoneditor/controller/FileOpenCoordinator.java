package com.daniel.jsoneditor.controller;

import java.io.File;
import com.daniel.jsoneditor.util.CanonicalPaths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single entry point for opening a JSON+schema file in a GUI window.
 * Routes to an existing window if the file is already open, otherwise delegates to
 * {@link AppService#openFileInNewWindowDirect(File, File)} to create a new window.
 */
public final class FileOpenCoordinator
{
    private static final Logger logger = LoggerFactory.getLogger(FileOpenCoordinator.class);

    private final WindowRegistry registry;

    private final AppService appService;

    public FileOpenCoordinator(final WindowRegistry registry, final AppService appService)
    {
        this.registry = registry;
        this.appService = appService;
    }

    /**
     * Opens the given file in a window. If a window is already showing this file,
     * focuses it instead of creating a new one.
     *
     * @param jsonFile   the JSON file to open
     * @param schemaFile the JSON Schema file to validate against
     */
    public void open(final File jsonFile, final File schemaFile)
    {
        final String canonicalPath = CanonicalPaths.canonicalize(jsonFile);

        registry.findByPath(canonicalPath).ifPresentOrElse(
                AppWindow::focus,
                () -> appService.openFileInNewWindowDirect(jsonFile, schemaFile));
    }
}
