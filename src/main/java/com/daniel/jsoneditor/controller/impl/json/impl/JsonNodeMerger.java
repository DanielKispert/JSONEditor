package com.daniel.jsoneditor.controller.impl.json.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeMerger
{
    public static JsonNode createMergedNode(JsonNode baseNode, JsonNode updateNode)
    {
        if (baseNode == null)
        {
            return updateNode;
        }
        if (updateNode == null)
        {
            return baseNode;
        }
        if (baseNode.isArray() && updateNode.isArray())
        {
            ArrayNode baseArray = (ArrayNode) baseNode;
            ArrayNode updateArray = (ArrayNode) updateNode;
            baseArray.addAll(updateArray); // TODO we can maybe replace this with equality checks later
        
            return baseArray;
        }
        else if (baseNode.isObject() && updateNode.isObject())
        {
            ObjectNode baseObject = (ObjectNode) baseNode;
            ObjectNode updateObject = (ObjectNode) updateNode;
        
            // Recursively merge object properties
            updateObject.fields().forEachRemaining(entry ->
            {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (baseObject.has(key))
                {
                    // If the key exists in baseObject, merge the values recursively
                    JsonNode mergedValue = createMergedNode(baseObject.get(key), value);
                    baseObject.set(key, mergedValue);
                }
                else
                {
                    // Otherwise, add the key-value pair from updateObject
                    baseObject.set(key, value);
                }
            });
        
            return baseObject;
        }
        else
        {
            // For other types, return the updateNode
            return updateNode;
        }
    }
}
