package com.daniel.jsoneditor.controller;

import java.io.File;
import java.util.List;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.diff.DiffEntry;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;


public interface Controller
{
    SettingsController getSettingsController();

    McpController getMcpController();

    /**
     * Gets the command manager for accessing command history
     * @return the command manager instance
     */
    CommandManager getCommandManager();

    /**
     * Undo the last action performed by the user.
     * If no action can be undone, this method does nothing.
     */
    void undo();

    /**
     * Redo the last undone action performed by the user.
     * If no action can be redone, this method does nothing.
     */
    void redo();


    void launchFinished();



    /** Loads the given JSON and schema files; {@code settings} may be {@code null} for no per-file settings. */
    void jsonAndSchemaSelected(final File json, final File schema, final File settings);

    /** Moves {@code item} to position {@code index} (0-based) within {@code newParent}. */
    void moveItemToIndex(final JsonNodeWithPath newParent, final JsonNodeWithPath item, final int index);

    /** Resolves {@code ${VAR}} placeholders in {@code json} by prompting the user for values; returns the result. */
    String resolveVariablesInJson(final String json);

    /** Merges {@code content} (a JSON string) into the node at {@code path}. */
    void importAtNode(final String path, final String content);

    /** Exports only the node at {@code path} to a file (no dependencies). */
    void exportNode(final String path);

    /** Exports the node at {@code path} together with all nodes it depends on. */
    void exportNodeWithDependencies(final String path);

    /** Removes all nodes at the given {@code paths} as a single undoable batch operation. */
    void removeNodes(final List<String> paths);

    void addNewNodeToArray(final String path);

    /** Creates a new referenceable object under {@code pathOfReferenceableObject} using {@code key} as its identifier. */
    void createNewReferenceableObjectNodeWithKey(final String pathOfReferenceableObject, final String key);

    void sortArray(final String path);

    /** Reorders the array at {@code path} according to {@code newIndices}, which must be a permutation of [0, size). */
    void reorderArray(final String path, final List<Integer> newIndices);

    void duplicateArrayNode(final String path);

    /** Duplicates the referenceable object at {@code pathToDuplicate} and links the copy to {@code referencePath}. */
    void duplicateReferenceableObjectForLinking(final String referencePath, final String pathToDuplicate);

    void saveToFile();

    void refreshFromDisk();

    /** Searches at/under {@code path} for a node matching {@code value}; returns the matching path or {@code null}. */
    String searchForNode(final String path, final String value);

    void openNewJson();

    void generateJson();

    /** Sets the value at {@code path}; {@code value} may be a {@link String}, {@link Number}, {@link Boolean}, or {@code null}. */
    void setValueAtPath(final String path, final Object value);
    
    /**
     * Sets a complete JSON node at the given path.
     * Used for reverting diff entries to their saved state.
     *
     * @param path The path to the node
     * @param node The JsonNode to set
     */
    void overrideNodeAtPath(final String path, final JsonNode node);
    
    /** Copies the JSON node at {@code path} to the system clipboard. */
    void copyToClipboard(final String path);
    /** Pastes the clipboard JSON, replacing the existing child node at {@code pathToInsert}. */
    void pasteFromClipboardReplacingChild(final String pathToInsert);

    /** Pastes the clipboard JSON as a new element appended to the array at {@code parentPath}. */
    void pasteFromClipboardIntoParent(final String parentPath);
    
    /**
     * Calculates differences between the JSON currently in the editor and the JSON saved on disk.
     *
     * @return List of differences, empty list if no differences found
     */
    List<DiffEntry> calculateJsonDiff();

    /**
     * Performs a clean shutdown: stops background services (e.g. MCP server) and releases resources.
     */
    void shutdown();

    /**
     * Manually triggers an update check against GitHub releases. Always shows a result toast.
     */
    void checkForUpdate();

    /**
     * Silently checks for updates (no toast if already on latest). Only runs once per session.
     */
    void checkForUpdateSilently();
}
