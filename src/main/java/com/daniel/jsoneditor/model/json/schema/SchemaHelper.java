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
}
