package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

public interface Controller
{
    
    void setFileProperties(boolean rememberPaths, String jsonPath, String schemaPath, String settingsPath);

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
    
    String getLastJsonPath();
    
    String getLastSchemaPath();
    
    String getLastSettingsPath();
    
    boolean getRememberPaths();
    
    void generateJson();
    

}
