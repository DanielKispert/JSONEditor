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
    
    public static String getTypeFromNode(JsonNode node)
    {
        if (node == null || node.get("type") == null)
        {
            return null;
        }
        return node.get("type").asText();
    }
    
    public static List<JsonNodeWithPath> getAllChildNodesFromSchema(JsonNodeWithPath node, JsonNode schema)
    {
        List<JsonNodeWithPath> childNodes = new ArrayList<>();
        List<String> requiredFields = getRequiredFields(schema);
        JsonNode properties = schema.get("properties");
        properties.fields().forEachRemaining(stringJsonNodeEntry ->
        {
            String key = stringJsonNodeEntry.getKey();
            JsonNode propertySchema = stringJsonNodeEntry.getValue();
            // we look for the property in the node
            JsonNode child = node.getNode().get(key);
            // if no child exists for that schema bit, generate an empty node for it already
            if (child == null)
            {
                child = NodeGenerator.generateNodeFromSchema(propertySchema);
            }
            String title = node.getPath() + "/" + key;
            if (requiredFields.contains(key))
            {
                title += "*";
            }
            childNodes.add(new JsonNodeWithPath(child, title));
        });
        return childNodes;
    }
    
    public static List<String> getRequiredFields(JsonNode schema)
    {
        List<String> requiredFields = new ArrayList<>();
        JsonNode required = schema.get("required");
        // the "required" node is an array of strings
        if (required != null && required.isArray())
        {
            for (JsonNode requiredField : required)
            {
                requiredFields.add(requiredField.toString());
            }
        }
        return requiredFields;
    }
    
    
    public static String findNodeWithValue(JsonNode rootNode, String path, String value)
    {
        String[] pathParts = path.split("\\?");
    
        if (pathParts.length > 2)
        {
            throw new IllegalArgumentException("Path contains more than one '?'");
        }
    
        String prefix = pathParts[0];
        // we need to format the prefix by removing the last "/" at the end of the string, otherwise it points to the wrong element
        if (prefix.length() > 0)
        {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
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
                    return prefix + "/" + i;
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
        return node != null && node.isInt() && node.asInt() == expectedValue;
    }
    
}
