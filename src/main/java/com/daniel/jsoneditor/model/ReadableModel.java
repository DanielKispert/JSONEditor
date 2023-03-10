package com.daniel.jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.settings.Settings;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
    JsonNode getRootJson();
    
    JsonSchema getRootSchema();
    
    Settings getSettings();
    
    /**
     * @return true if the currently selected node is an array and can have more items, false if not
     */
    boolean canAddMoreItems(String path);
    
    String searchForNode(String path, String value);
    
    JsonNodeWithPath getNodeForPath(String path);
    
    JsonNode getSubschemaForPath(String path);
    
}
