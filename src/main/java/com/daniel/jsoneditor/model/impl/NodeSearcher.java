package com.daniel.jsoneditor.model.impl;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;

import java.util.ArrayList;
import java.util.List;

public class NodeSearcher
{
    public static List<JsonNodeWithPath> getAllChildNodesFromSchema(JsonSchema rootSchema, JsonNodeWithPath node, JsonNode schema)
    {
        List<JsonNodeWithPath> childNodes = new ArrayList<>();
        JsonNode properties = schema.get("properties");
        properties.fields().forEachRemaining(stringJsonNodeEntry ->
        {
            String key = stringJsonNodeEntry.getKey();
            JsonNode propertySchema = SchemaHelper.getSchemaNodeResolvingRefs(rootSchema, stringJsonNodeEntry.getValue());
            // we look for the property in the node
            JsonNode child = node.getNode().get(key);
            // if no child exists for that schema bit, generate an empty node for it already
            if (child == null)
            {
                child = NodeGenerator.generateNodeFromSchema(propertySchema);
            }
            childNodes.add(new JsonNodeWithPath(child, node.getPath() + "/" + key));
        });
        return childNodes;
    }
    
    
    public static String findNodeWithValue(JsonNode rootNode, String path, String value)
    {
        String[] pathParts = path.split("\\?");
    
        if (pathParts.length > 2)
        {
            throw new IllegalArgumentException("Path contains more than one '?'");
        }
    
        String prefix = pathParts[0];
        String suffix = pathParts.length > 1 ? pathParts[1] : "";
    
        JsonNode parentNode = rootNode.at(prefix);
    
        if (parentNode.isArray())
        {
            int valueAsInt = Integer.parseInt(value);
            for (int i = 0; i < parentNode.size(); i++)
            {
                JsonNode childNode = parentNode.get(i);
            
                if (doesNodeMatchValue(childNode, suffix, valueAsInt))
                {
                    return prefix + "/" + i + suffix;
                }
            }
        }
        else if (parentNode.isObject())
        {
            JsonNode childNode = parentNode.get(suffix);
        
            if (doesNodeMatchValue(childNode, "", value))
            {
                return prefix + "/" + suffix;
            }
        }
        else
        {
            throw new IllegalArgumentException("Parent node is not an array or object");
        }
    
        return null;
        
    }
    
    
    private static boolean doesNodeMatchValue(JsonNode rootNode, String jsonPointer, String expectedValue)
    {
        JsonNode node = rootNode.at(JsonPointer.valueOf(jsonPointer));
        if (node != null && node.isTextual() && node.asText().equals(expectedValue))
        {
            return true;
        }
        return false;
    }
    
    private static boolean doesNodeMatchValue(JsonNode rootNode, String jsonPointer, int expectedValue)
    {
        JsonNode node = rootNode.at(JsonPointer.valueOf(jsonPointer));
        if (node != null && node.isInt() && node.asInt() == expectedValue)
        {
            return true;
        }
        return false;
    }
    
}
