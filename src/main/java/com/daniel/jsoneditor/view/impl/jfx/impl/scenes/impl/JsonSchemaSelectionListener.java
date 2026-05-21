package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import java.io.File;

/**
 * Callback invoked by {@link JSONSelectionScene} when the user confirms a JSON + schema
 * file selection. Decouples the picker scene from the controller.
 */
@FunctionalInterface
public interface JsonSchemaSelectionListener
{
    /**
     * Called when the user clicks OK in the file-selection scene.
     *
     * @param jsonFile     the JSON file to edit (may be a new empty file)
     * @param schemaFile   the JSON Schema file
     * @param settingsFile the optional settings JSON file
     */
    void onFilesSelected(File jsonFile, File schemaFile, File settingsFile);
}
