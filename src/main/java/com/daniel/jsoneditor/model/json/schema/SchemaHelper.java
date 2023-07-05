package com.daniel.jsoneditor.model.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;

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
    
    public static String getLastPathSegment(String path)
    {
        String[] segments = path.split("/");
        return segments[segments.length - 1];
    }
    
    public static String incrementLastPathSegment(String path)
    {
        String[] segments = path.split("/");
        int lastSegmentIndex = segments.length - 1;
        String lastSegment = segments[lastSegmentIndex];
        int incrementedValue = Integer.parseInt(lastSegment) + 1;
        segments[lastSegmentIndex] = String.valueOf(incrementedValue);
        return String.join("/", segments);
    }
}
