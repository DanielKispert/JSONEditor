package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.model.ReadableModel;

import java.io.File;


/**
 * Represents an active editor session.
 *
 * @param id unique session identifier (never null)
 * @param model the readable model for this session (never null)
 * @param jsonFile the JSON file being edited (may be null for unsaved/new documents)
 * @param schemaFile the schema file (may be null if no schema)
 * @param guiOwned true if this session is owned by a GUI window (cannot be closed via MCP)
 */
public record EditorSession(String id, ReadableModel model, File jsonFile, File schemaFile, boolean guiOwned)
{
}
