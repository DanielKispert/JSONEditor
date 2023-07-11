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
        switch (SchemaHelper.getType(schema))
        {
            case "array":
                JsonNode itemSchema = schema.get("items");
                int maxItems = schema.has("maxItems") ? schema.get("maxItems").asInt() : 0;
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                for (int i = 0; i < maxItems; i++)
                {
                    arrayNode.add(generateNodeFromSchema(itemSchema));
                }
                return arrayNode;
            case "object":
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                List<String> requiredProperties = SchemaHelper.getRequiredProperties(schema);
                JsonNode properties = schema.get("properties");
                properties.fields().forEachRemaining(entry ->
                {
                    String key = entry.getKey();
                    JsonNode childSchema = entry.getValue();
                    String childSchemaType = SchemaHelper.getType(childSchema);
                    // we want to generate child objects for every node that is either required or also an object or array (because we can't fill those in via fields yet)
                    if (requiredProperties.contains(key) || "array".equals(childSchemaType) || "object".equals(childSchemaType))
                    {
                        objectNode.set(key, generateNodeFromSchema(childSchema));
                    }
                });
                return objectNode;
            case "string":
                return JsonNodeFactory.instance.textNode("");
            case "integer":
            case "number":
                return JsonNodeFactory.instance.numberNode(0);
            case "boolean":
                return JsonNodeFactory.instance.booleanNode(true);
            default:
                return JsonNodeFactory.instance.nullNode();
        }
    }
}
