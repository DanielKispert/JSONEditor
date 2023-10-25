package com.daniel.jsoneditor.model.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class NodeStructureDelegate
{
    private static final String INTEGER_REGEX = "^\\d+$";
    
    public static JsonNode getExportStructureForNodes(ReadableModel model, List<String> paths)
    {
        if (paths == null || paths.isEmpty())
        {
            return null;
        }
        JsonNode structure = null;
        for (String path : paths)
        {
            String cleanedPath = path.substring(1); // the first slash is not needed
            String[] pathElements = cleanedPath.split("/");
            Iterator<String> elementsIterator = Arrays.stream(pathElements).iterator();
            structure = getPartialStructure(model, structure, path, elementsIterator);
        }
        System.out.println("Created export structure for " + paths.size() + " nodes");
        return structure;
    }
    
    private static JsonNode getPartialStructure(ReadableModel model, JsonNode existingStructure, String path, Iterator<String> elementsIterator)
    {
        if (elementsIterator.hasNext())
        {
            JsonNodeFactory factory = JsonNodeFactory.instance;
            String nextElement = elementsIterator.next();
            JsonNode parentOfPath;
            // Check if the element is an integer (array index)
            if (nextElement.matches(INTEGER_REGEX))
            {
                if (existingStructure != null && existingStructure.isArray())
                {
                    parentOfPath = existingStructure;
                }
                else
                {
                    parentOfPath = factory.arrayNode();
                }
                ((ArrayNode) parentOfPath).add(getPartialStructure(model, parentOfPath.get(Integer.parseInt(nextElement)), path, elementsIterator));
            }
            else
            {
                if (existingStructure != null && existingStructure.isObject())
                {
                    parentOfPath = existingStructure;
                }
                else
                {
                    parentOfPath = factory.objectNode();
                }
                ((ObjectNode) parentOfPath).set(nextElement, getPartialStructure(model, parentOfPath.get(nextElement), path, elementsIterator));
            }
            return parentOfPath;
        }
        return model.getNodeForPath(path).getNode();
    }
}
