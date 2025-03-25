package com.daniel.jsoneditor.controller.impl.json.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeMerger
{
    public static JsonNode createMergedNode(ReadableModel model, JsonNodeWithPath baseNode, JsonNode updateNode)
    {
        if (baseNode == null)
        {
            return updateNode;
        }
        if (updateNode == null)
        {
            return baseNode.getNode();
        }
        if (baseNode.isArray() && updateNode.isArray())
        {
            ArrayNode baseArray = (ArrayNode) baseNode.getNode();
            ArrayNode updateArray = (ArrayNode) updateNode;
            
            // we want the base array to contain all elements of it, merge all elements that were in the update array, too
            // and add all elements of the update array that werent in the base array
            for (JsonNode updateElement : updateArray)
            {
                // if the update element is not an object or array we'll just add it to the base array
                if (!updateElement.isObject() && !updateElement.isArray())
                {
                    baseArray.add(updateElement);
                    continue;
                }
                // if the update element is an object or array we'll try to merge it into the base array if it's already in there
                // we do merging via the ReferenceableObject framework
                boolean containedInBaseArray = false;
                JsonNode baseElementToMerge = null;
                int foundIndex;
                for (foundIndex = 0; foundIndex < baseArray.size(); foundIndex++)
                {
                    baseElementToMerge = baseArray.get(foundIndex);
                    model.getReferenceableObject(baseNode.getPath() + "/" + foundIndex);
                    if (baseElementToMerge.equals(updateElement)) //TODO fix equality
                    {
                        containedInBaseArray = true;
                        break;
                    }
                }
                if (containedInBaseArray)
                {
                    JsonNode mergedElement = createMergedNode(model,
                            new JsonNodeWithPath(baseElementToMerge, baseNode.getPath() + "/" + foundIndex), updateElement);
                    baseArray.set(foundIndex, mergedElement);
                }
                else
                {
                    baseArray.add(updateElement);
                }
            }
            
            return baseArray;
        }
        else if (baseNode.isObject() && updateNode.isObject())
        {
            ObjectNode baseObject = (ObjectNode) baseNode.getNode();
            ObjectNode updateObject = (ObjectNode) updateNode;
        
            // Recursively merge object properties
            updateObject.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (baseObject.has(key))
                {
                    // If the key exists in baseObject, merge the values recursively
                    JsonNode mergedValue = createMergedNode(model,
                            new JsonNodeWithPath(baseObject.get(key), baseNode.getPath() + "/" + key), value);
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
