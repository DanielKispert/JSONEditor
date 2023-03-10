package com.daniel.jsoneditor.model.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

public class SchemaHelper
{
    
    
    public static JsonNode getSchemaNodeResolvingRefs(JsonSchema root, JsonSchema possibleRef)
    {
        return getSchemaNodeResolvingRefs(root, possibleRef.getSchemaNode());
    }
    
    public static JsonNode getSchemaNodeResolvingRefs(JsonSchema root, JsonNode node)
    {
        JsonNode ref = node.get("$ref");
        if (ref != null)
        {
            return root.getRefSchemaNode(ref.asText());
        }
        else
        {
            return node;
        }
    }
}
