package com.daniel.jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * methods that should not be callable by anything outside the model
 */
public interface WritableModelInternal extends WritableModel
{
    /**
     * @return the index where the node was added or -1 if it wasn't added
     */
    int addNodeToArray(String selectedPath, JsonNode content);
    
    /**
     * Generates a new element conforming to array item schema for the array at selectedPath.
     */
    JsonNode makeArrayNode(String selectedPath);
}
