package com.daniel.jsoneditor.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class JsonNodeHelperTest
{
    @Test
    void testNullReturnsNullNode()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode(null);
        assertInstanceOf(NullNode.class, result, "Expected NullNode for null input");
    }

    @Test
    void testBooleanReturnsBoolean()
    {
        final JsonNode trueResult = JsonNodeHelper.toJsonNode(true);
        assertInstanceOf(BooleanNode.class, trueResult);
        assertTrue(trueResult.booleanValue());

        final JsonNode falseResult = JsonNodeHelper.toJsonNode(false);
        assertInstanceOf(BooleanNode.class, falseResult);
        assertFalse(falseResult.booleanValue());
    }

    @Test
    void testIntegerReturnsNumber()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode(42);
        assertInstanceOf(NumericNode.class, result);
        assertEquals(42, result.intValue());
    }

    @Test
    void testLongReturnsNumber()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode(123456789L);
        assertInstanceOf(NumericNode.class, result);
        assertEquals(123456789L, result.longValue());
    }

    @Test
    void testDoubleReturnsNumber()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode(3.14);
        assertInstanceOf(NumericNode.class, result);
        assertEquals(3.14, result.doubleValue(), 0.0001);
    }

    @Test
    void testFloatReturnsNumber()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode(2.5f);
        assertInstanceOf(NumericNode.class, result);
        assertEquals(2.5, result.doubleValue(), 0.0001);
    }

    @Test
    void testStringReturnsText()
    {
        final JsonNode result = JsonNodeHelper.toJsonNode("hello");
        assertInstanceOf(TextNode.class, result);
        assertEquals("hello", result.textValue());
    }

    @Test
    void testUnknownObjectReturnsToString()
    {
        final Object obj = new Object()
        {
            @Override
            public String toString()
            {
                return "custom-object";
            }
        };
        final JsonNode result = JsonNodeHelper.toJsonNode(obj);
        assertInstanceOf(TextNode.class, result);
        assertEquals("custom-object", result.textValue());
    }
}
