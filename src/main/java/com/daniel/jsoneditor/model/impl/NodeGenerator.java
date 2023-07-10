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
        switch (schema.get("type").asText())
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
                    JsonNode value = entry.getValue();
                    if (requiredProperties.contains(key))
                    {
                        objectNode.set(key, generateNodeFromSchema(value));
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
