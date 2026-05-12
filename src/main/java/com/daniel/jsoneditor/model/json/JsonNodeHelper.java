package com.daniel.jsoneditor.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;


/**
 * Utility class for converting Java values into Jackson {@link JsonNode} instances.
 */
public final class JsonNodeHelper
{
    private JsonNodeHelper()
    {
        // utility class
    }

    /**
     * Converts a Java value to a Jackson JsonNode.
     * Handles null, Boolean, Integer, Long, Double, Number (fallback), and toString for everything else.
     */
    public static JsonNode toJsonNode(final Object value)
    {
        if (value == null)
        {
            return JsonNodeFactory.instance.nullNode();
        }
        if (value instanceof Boolean)
        {
            return JsonNodeFactory.instance.booleanNode((Boolean) value);
        }
        if (value instanceof Integer)
        {
            return JsonNodeFactory.instance.numberNode((Integer) value);
        }
        if (value instanceof Long)
        {
            return JsonNodeFactory.instance.numberNode((Long) value);
        }
        if (value instanceof Double)
        {
            return JsonNodeFactory.instance.numberNode((Double) value);
        }
        if (value instanceof Number)
        {
            return JsonNodeFactory.instance.numberNode(((Number) value).doubleValue());
        }
        return JsonNodeFactory.instance.textNode(value.toString());
    }
}
