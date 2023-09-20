package com.daniel.jsoneditor.model.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SchemaHelper
{
    public static JsonSchema resolveJsonRefsInSchema(JsonSchema root)
    {
        JsonNode schemaNode = root.getSchemaNode();
        
        // Recursively resolve all JSON references in the schema
        resolveJsonRefs(root, schemaNode);
        
        return root;
    }
    
    private static void resolveJsonRefs(JsonSchema rootWithRefs, JsonNode node)
    {
        if (node.isObject())
        {
            // Check if the object contains a "$ref" property
            JsonNode refNode = node.get("$ref");
            if (refNode != null && refNode.isTextual())
            {
                ObjectNode objectNode = (ObjectNode) node;
                String ref = refNode.textValue();
                // Resolve the JSON reference and replace the "$ref" node with the referenced schema node
                JsonNode resolvedRefNode = rootWithRefs.getRefSchemaNode(ref);
                objectNode.setAll((ObjectNode) resolvedRefNode);
                objectNode.remove("$ref");
                // Recursively resolve all JSON references in the referenced schema
                resolveJsonRefs(rootWithRefs, resolvedRefNode);
            }
            else
            {
                // Recursively resolve all JSON references in the object's properties
                node.fields().forEachRemaining(entry -> resolveJsonRefs(rootWithRefs, entry.getValue()));
            }
        }
        else if (node.isArray())
        {
            // Recursively resolve all JSON references in the array's items
            node.elements().forEachRemaining(item -> resolveJsonRefs(rootWithRefs, item));
        }
    }
    
    public static String getParentPath(String path)
    {
        if (path == null || path.isEmpty())
        {
            return null;
        }
        
        // Remove the trailing slash if it exists
        String trimmedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        
        // Find the last index of '/'
        int lastIndex = trimmedPath.lastIndexOf('/');
        if (lastIndex >= 0)
        {
            return trimmedPath.substring(0, lastIndex);
        }
        
        return null;
    }
    
    public static List<String> getRequiredProperties(JsonNode schemaNode)
    {
        List<String> requiredProperties = new ArrayList<>();
        JsonNode requiredPropertiesNode = schemaNode.get("required");
        if (requiredPropertiesNode != null && requiredPropertiesNode.isArray())
        {
            for (JsonNode requiredProperty : requiredPropertiesNode)
            {
                if (requiredProperty.isTextual())
                {
                    requiredProperties.add(requiredProperty.asText());
                }
            }
        }
        return requiredProperties;
    }
    
    public static String getLastPathSegment(String path)
    {
        String[] segments = path.split("/");
        return segments[segments.length - 1];
    }
    
    public static List<String> getTypes(JsonNode schema)
    {
        if (schema != null)
        {
            JsonNode typeNode = schema.get("type");
            if (typeNode != null)
            {
                if (typeNode.isArray())
                {
                    List<String> types = new ArrayList<>();
                    for (JsonNode type : typeNode)
                    {
                        types.add(type.asText());
                    }
                    return types;
                }
                else
                {
                    return new ArrayList<>(Collections.singleton(typeNode.asText()));
                }
            }
        }
        return null;
    }
    
}
