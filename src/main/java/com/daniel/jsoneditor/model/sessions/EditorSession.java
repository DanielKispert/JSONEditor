package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.model.ReadableModel;

import java.io.File;


/**
 * Represents a single open file session with its model and metadata.
 *
 * @param id unique session identifier
 * @param model the model for this session
 * @param jsonFile the JSON file being edited
 * @param schemaFile the schema file used for validation
 * @param guiOwned true if this session is owned by the GUI (cannot be closed via MCP)
 */
public record EditorSession(String id, ReadableModel model, File jsonFile, File schemaFile, boolean guiOwned)
{
}
