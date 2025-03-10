package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.settings.Settings;

import java.io.File;
import java.util.List;


public interface WritableModel
{
    
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void refreshJsonNode(JsonNode jsonNode);
    
    void setSettings(Settings settings);
    
    void sendEvent(Event state);
    
    void moveItemToIndex(JsonNodeWithPath item, int index);
    
    /**
     * @param selectedPath points to the path of the array to which a node should be added
     */
    void addNodeToArray(String selectedPath);
    
    /**
     * @return the index where the node was added or -1 if it wasn't added
     */
    int addNodeToArray(String selectedPath, JsonNode content);
    
    /**
     * sorts the array node at the given path
     * @param path
     */
    void sortArray(String path);
    
    /**
     * duplicates the array item the path points to
     */
    void duplicateArrayItem(String pathToItemToDuplicate);
    
    /**
     * duplicates a referenceable object that should be linked in a reference and adjusts the reference with its name
     */
    void duplicateNodeAndLink(String referencePath, String pathToItemToDuplicate);
    
    void removeNodes(List<String> paths);
    
    void removeNode(String path);
    
    /**
     * sets a path to a node, replacing everything that was there before
     */
    void setNode(String path, JsonNode content);
    
    /**
     * adds a new node to the JSON for the
     */
    void addReferenceableObjectNodeWithKey(String pathOfReferenceableObject, String key);
}
