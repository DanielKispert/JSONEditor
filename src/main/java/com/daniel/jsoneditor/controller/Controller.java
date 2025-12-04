package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.diff.DiffEntry;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.List;


public interface Controller
{
    /*
     * Other controller methods for nested controllers
     */
    SettingsController getSettingsController();
    
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
    
    void jsonAndSchemaSelected(File json, File schema, File settings);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    String resolveVariablesInJson(String json);
    
    void importAtNode(String path, String content);
    
    void exportNode(String path);
    
    void exportNodeWithDependencies(String path);
    
    void removeNodes(List<String> paths);
    
    void addNewNodeToArray(String path);
    
    void createNewReferenceableObjectNodeWithKey(String pathOfReferenceableObject, String key);
    
    void sortArray(String path);
    
    void duplicateArrayNode(String path);
    
    void duplicateReferenceableObjectForLinking(String referencePath, String pathToDuplicate);
    
    void saveToFile();
    
    void refreshFromDisk();
    
    String searchForNode(String path, String value);
    
    void openNewJson();
    
    void generateJson();
    
    void setValueAtPath(String path, Object value);
    
    /**
     * Sets a complete JSON node at the given path.
     * Used for reverting diff entries to their saved state.
     *
     * @param path The path to the node
     * @param node The JsonNode to set
     */
    void overrideNodeAtPath(String path, JsonNode node);
    
    /**
     * copy the node at the path to the clipboard
     */
    void copyToClipboard(String path);
    
    void pasteFromClipboardReplacingChild(String pathToInsert);
    
    void pasteFromClipboardIntoParent(String parentPath);
    
    /**
     * Calculates differences between the JSON currently in the editor and the JSON saved on disk.
     *
     * @return List of differences, empty list if no differences found
     */
    List<DiffEntry> calculateJsonDiff();

}
