package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.view.impl.jfx.PathDisplayConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scenario tests for {@link PathDisplayConverter#convertToDisplay}.
 */
public class PathDisplayConverterTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private static final String SCHEMA_JSON = "{\"type\":\"object\","
            + "\"properties\":{\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"id\":{\"type\":\"string\"}}}}}}";

    @Test
    void nullPath_returnsNull()
    {
        final ModelImpl model = buildModel();

        assertNull(PathDisplayConverter.convertToDisplay(model, null), "null path should return null");
    }

    @Test
    void missingNode_returnsRawPath()
    {
        final ModelImpl model = buildModel();

        final String result = PathDisplayConverter.convertToDisplay(model, "/items/999");
        assertEquals("/items/999", result, "Missing node should fall back to raw path");
    }

    @Test
    void arrayIndexPath_returnsReadableDisplayPath()
    {
        final ModelImpl model = buildModel();

        final String result = PathDisplayConverter.convertToDisplay(model, "/items/0");

        assertFalse(result.contains("/0"), "Display path must not contain raw array index '/0': " + result);
        assertTrue(result.contains("item_a"), "Display path must contain item display name 'item_a': " + result);
    }

    // --- helpers ---

    private static ModelImpl buildModel()
    {
        final ObjectNode data = MAPPER.createObjectNode();
        final ArrayNode items = MAPPER.createArrayNode();
        final ObjectNode item = MAPPER.createObjectNode();
        item.put("id", "item_a");
        items.add(item);
        data.set("items", items);

        final JsonSchema schema;
        try
        {
            schema = SCHEMA_FACTORY.getSchema(MAPPER.readTree(SCHEMA_JSON));
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to parse test schema", e);
        }
        final ModelImpl model = ModelFactory.createEmpty();
        model.jsonAndSchemaSuccessfullyValidated(
                new File("dummy.json"), new File("dummy_schema.json"), data, schema);
        return model;
    }
}
