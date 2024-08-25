package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;

import java.io.File;

public interface Controller
{
    /*
     * Other controller methods for nested controllers
     */
    SettingsController getSettingsController();
    

    void launchFinished();
    
    void jsonAndSchemaSelected(File json, File schema, File settings);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    String resolveVariablesInJson(String json);
    
    void importAtNode(String path, String content);
    
    void exportNode(String path);
    
    void exportNodeWithDependencies(String path);
    
    void removeNode(String path);
    
    void addNewNodeToArray(String path);
    
    void createNewReferenceableObjectNodeWithKey(String pathOfReferenceableObject, String key);
    
    void sortArray(String path);
    
    void duplicateArrayNode(String path);
    
    void duplicateReferenceableObjectForLinking(String referencePath, String pathToDuplicate);
    
    void saveToFile();
    
    void refreshFromFile();
    
    String searchForNode(String path, String value);
    
    void openNewJson();
    
    void generateJson();
    
    /**
     * copy the node at the path to the clipboard
     */
    void copyToClipboard(String path);
    
    void pasteFromClipboardReplacingChild(String pathToInsert);
    
    void pasteFromClipboardIntoParent(String parentPath);
    

}
