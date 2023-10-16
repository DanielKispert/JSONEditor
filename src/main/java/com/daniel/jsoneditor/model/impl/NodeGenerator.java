package com.daniel.jsoneditor.model.impl;

import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class NodeGenerator
{
    public static JsonNode generateNodeFromSchema(JsonNode schema)
    {
        List<String> types = SchemaHelper.getTypes(schema);
        if (types.contains("array"))
        {
            return generateArrayNode(schema);
        }
        else if (types.contains("object"))
        {
            return generateObjectNode(schema);
        }
        else if (types.contains("boolean"))
        {
            return JsonNodeFactory.instance.booleanNode(true);
        }
        else if (types.contains("string"))
        {
            return JsonNodeFactory.instance.textNode("");
        }
        else if (types.contains("integer") || types.contains("number"))
        {
            return JsonNodeFactory.instance.numberNode(0);
        }
        else
        {
            return JsonNodeFactory.instance.nullNode();
        }
    }
    
    private static JsonNode generateArrayNode(JsonNode schema)
    {
        JsonNode itemSchema = schema.get("items");
        int maxItems = schema.has("minItems") ? schema.get("minItems").asInt() : 0;
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < maxItems; i++)
        {
            arrayNode.add(generateNodeFromSchema(itemSchema));
        }
        return arrayNode;
    }
    
    private static JsonNode generateObjectNode(JsonNode schema)
    {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        List<String> requiredProperties = SchemaHelper.getRequiredProperties(schema);
        JsonNode properties = schema.get("properties");
        properties.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode childSchema = entry.getValue();
            List<String> childSchemaTypes = SchemaHelper.getTypes(childSchema);
            // we want to generate child objects for every node that is either required or also an object or array (because we can't fill those in via fields yet)
            if (requiredProperties.contains(key) || (childSchemaTypes != null && (childSchemaTypes.contains("array")
                    || childSchemaTypes.contains("object"))))
            {
                objectNode.set(key, generateNodeFromSchema(childSchema));
            }
        });
        return objectNode;
    }
}
