package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
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
    
    ReferenceToObject getReferenceToObject(String path);
    
    /**
     * @return prepared referenceable object instances
     */
    List<ReferenceableObjectInstance> getReferenceableObjectInstances();
    
    String searchForNode(String path, String value);
    
    JsonNodeWithPath getNodeForPath(String path);
    
    /**
     * returns the given nodes in a JSON structure based on their parts. All other nodes are empty.
     * @param paths the paths which should be put into the empty structure
     * @return a JsonNode with the given nodes as its children (as much as possible)
     */
    JsonNode getExportStructureForNodes(List<String> paths);
    
    /**
     * returns the "dependent nodes" of a node, which is references to other object nodes
     */
    List<String> getDependentPaths(JsonNodeWithPath node);
    
    List<ReferenceableObject> getReferenceableObjects();
    
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
