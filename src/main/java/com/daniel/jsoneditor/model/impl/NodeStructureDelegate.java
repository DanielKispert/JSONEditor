package com.daniel.jsoneditor.model.impl;

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
    
    public static JsonNode getExportStructureForNodes(List<JsonNodeWithPath> nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            return null;
        }
        JsonNode structure = null;
        for (JsonNodeWithPath node : nodes)
        {
            String cleanedPath = node.getPath().substring(1); // the first slash is not needed
            String[] pathElements = cleanedPath.split("/");
            Iterator<String> elementsIterator = Arrays.stream(pathElements).iterator();
            structure = getPartialStructure(structure, node, elementsIterator);
        }
        return structure;
    }
    
    private static JsonNode getPartialStructure(JsonNode existingStructure, JsonNodeWithPath node, Iterator<String> elementsIterator)
    {
        if (elementsIterator.hasNext())
        {
            JsonNodeFactory factory = JsonNodeFactory.instance;
            String nextElement = elementsIterator.next();
            JsonNode parentOfPath;
            // Check if the element is an integer (array index)
            if (nextElement.matches(INTEGER_REGEX))
            {
                // the first node must be an array then
                parentOfPath = factory.arrayNode();
                ((ArrayNode) parentOfPath).add(getPartialStructure(node, elementsIterator));
            }
            else
            {
                // the parent of this path is an object
                parentOfPath = factory.objectNode();
                ((ObjectNode) parentOfPath).set(nextElement, getPartialStructure(node, elementsIterator));
            }
            return parentOfPath;
        }
        return node.getNode();
    }
}
