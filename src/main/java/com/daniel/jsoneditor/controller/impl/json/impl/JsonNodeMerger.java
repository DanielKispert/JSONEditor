package com.daniel.jsoneditor.controller.impl.json.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.Instanceable;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
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
            return mergeArrays(model, baseNode, (ArrayNode) updateNode);
        }
        else if (baseNode.isObject() && updateNode.isObject())
        {
            return mergeObjects(model, baseNode, (ObjectNode) updateNode);
        }
        else
        {
            // For other types, return the updateNode
            return updateNode;
        }
    }
    
    private static ArrayNode mergeArrays(ReadableModel model, JsonNodeWithPath baseNode, ArrayNode updateNode)
    {
        ArrayNode baseArray = (ArrayNode) baseNode.getNode();
        // check if the base node is an array of referenceable objects
        Instanceable arrayOfObjectsOrReferences = getInstanceableOfArray(model, baseNode.getPath());
        
        // we want the base array to contain all elements of it, merge all elements that were in the update array, too
        // and add all elements of the update array that werent in the base array
        // we know that an element is the same
        for (JsonNode updateElement : updateNode)
        {
            // if the update element is not an object or array we'll just add it to the base array
            if (!updateElement.isObject() && !updateElement.isArray())
            {
                baseArray.add(updateElement);
                continue;
            }
            String keyOfUpdateElement = getComparisonKey(arrayOfObjectsOrReferences, updateElement);
            if (keyOfUpdateElement == null)
            {
                // the updateElement is null, skip this one
                continue;
            }
            // if the update element is an object or array we'll try to merge it into the base array if it's already in there
            // we do merging via the ReferenceableObject framework or via object equality (hashing)
            boolean containedInBaseArray = false;
            JsonNode baseElementToMerge = null;
            int foundIndex;
            for (foundIndex = 0; foundIndex < baseArray.size(); foundIndex++)
            {
                String keyOfObject = getComparisonKey(arrayOfObjectsOrReferences, baseArray.get(foundIndex));
                baseElementToMerge = baseArray.get(foundIndex);
                
                if (keyOfUpdateElement.equals(keyOfObject))
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
    
    private static Instanceable getInstanceableOfArray(ReadableModel model, String path)
    {
        ReferenceToObject referenceToObject = model.getReferenceToObject(path);
        if (referenceToObject != null)
        {
            return referenceToObject;
        }
        return model.getReferenceableObject(path);
    }
    
    private static String getComparisonKey(Instanceable instanceable, JsonNode possibleInstance)
    {
        if (possibleInstance == null)
        {
            return null;
        }
        if (instanceable == null)
        {
            return possibleInstance.hashCode() + "";
        }
        return instanceable.getInstanceIdentifier(possibleInstance);
    }
    
    private static ObjectNode mergeObjects(ReadableModel model, JsonNodeWithPath baseNode, ObjectNode updateNode)
    {
        ObjectNode baseObject = (ObjectNode) baseNode.getNode();
        // Recursively merge object properties
        updateNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (baseObject.has(key))
            {
                // If the key exists in baseObject, merge the values recursively
                JsonNode mergedValue = createMergedNode(model, new JsonNodeWithPath(baseObject.get(key), baseNode.getPath() + "/" + key),
                        value);
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
    
}
