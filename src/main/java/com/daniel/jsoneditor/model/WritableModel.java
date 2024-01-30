package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.settings.Settings;

import java.io.File;

public interface WritableModel
{
    
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void refreshJsonNode(JsonNode jsonNode);
    
    void setSettings(Settings settings);
    
    void sendEvent(Event state);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    /**
     * @param selectedPath points to the path of the array to which a node should be added
     */
    void addNodeToArray(String selectedPath);
    
    void addNodeToArray(String arrayPath, JsonNode nodeToAdd);
    
    /**
     * sorts the array node at the given path
     * @param path
     */
    void sortArray(String path);
    
    /**
     * duplicates the array item the path points to
     */
    void duplicateArrayItem(String pathToItemToDuplicate);
    
    void removeNode(String path);
    
    /**
     * sets a path to a node, replacing everything that was there before
     */
    void setNode(String path, JsonNode content);
}
