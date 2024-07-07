package com.daniel.jsoneditor.model;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
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
    
    /**
     * @return if the node at the given path has a ReferenceToObject, return it, otherwise return null.
     * If the node is an element of an array, the referenceToObject's path will refer to the array, so we must adjust the path
     */
    ReferenceToObject getReferenceToObject(String path);
    
    ReferenceableObject getReferenceableObject(String path);
    
    /**
     * @return a graph of the path and its direct incoming and outgoing references
     */
    Digraph<String, EdgeIdentifier> getJsonAsGraph(String path);
    
    /**
     * @param path this points to the path of a json node inside our currently selected json. It can match an existing node, or it doesn't.
     * It can also be a referenceable object or not.
     * @return the paths of all nodes that are a ReferenceToObject of the referenceableObjectInstance at that path
     */
    List<ReferenceToObjectInstance> getReferencesToObjectForPath(String path);
    
    /**
     * @return prepared referenceable object instances
     */
    List<ReferenceableObjectInstance> getReferenceableObjectInstances();
    
    List<ReferenceableObjectInstance> getInstancesOfReferenceableObjectAtPath(String referenceableObjectPath);
    
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
