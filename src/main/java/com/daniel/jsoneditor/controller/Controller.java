package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

public interface Controller
{
    /*
     * Other controller methods for nested controllers
     */
    SettingsController getSettingsController();
    
    /*
     * Methods for the view to call
     */

    void launchFinished();
    
    void jsonAndSchemaSelected(File json, File schema, File settings);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    String resolveVariablesInJson(String json);
    
    void importAtNode(String path, String content);
    
    void exportNode(String path);
    
    void exportNodeWithDependencies(String path);
    
    void removeNode(String path);
    
    void addNewNodeToArray(String path);
    
    void sortArray(String path);
    
    void duplicateArrayNode(String path);
    
    void saveToFile();
    
    void refreshFromFile();
    
    String searchForNode(String path, String value);
    
    void openNewJson();
    
    void generateJson();
    

}
