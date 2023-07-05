package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.settings.Settings;

import java.io.File;

public interface WritableModel
{
    
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void setSettings(Settings settings);
    
    void sendEvent(Event state);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    /**
     * @param selectedPath points to the path of the array to which a node should be added
     */
    void addNodeToArray(String selectedPath);
    
    /**
     * duplicates the array item the path points to
     */
    void duplicateArrayItem(String pathToItemToDuplicate);
    
    void removeNode(String path);
}
