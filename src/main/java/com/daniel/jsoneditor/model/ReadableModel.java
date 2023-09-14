package com.daniel.jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.settings.Settings;

import java.io.File;
import java.util.List;

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
    
    /**
     * @return a list of strings that holds example values that could be filled into the TextNode at the path. Only makes sense if the path is a TextNode, otherwise nothing is returned
     */
    List<String> getStringExamplesForPath(String path);
    
    /**
     * @return the allowed string values the path can take
     */
    List<String> getAllowedStringValuesForPath(String path);
    
    JsonSchema getSubschemaForPath(String path);
    
    String getIdentifier(String pathOfParent, JsonNode child);
    
}
