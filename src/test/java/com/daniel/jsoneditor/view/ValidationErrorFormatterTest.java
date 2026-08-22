package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.validation.ValidationError;
import com.daniel.jsoneditor.view.impl.jfx.toast.ValidationErrorFormatter;
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
 * Scenario tests for {@link ValidationErrorFormatter#format}.
 * Verifies that the view layer composes the user-facing message:
 * display name at the error location, raw path for the referenced object.
 */
public class ValidationErrorFormatterTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private static final String SCHEMA_JSON = "{\"type\":\"object\","
            + "\"properties\":{\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"id\":{\"type\":\"string\"}}}}}}";

    @Test
    void danglingReference_messageUsesDisplayName_andRawReferencedPath()
    {
        final ModelImpl model = buildModel();
        // error location: /items/0 (= item_a); target could not be resolved
        final ValidationError error = ValidationError.danglingReference(
                "/items/0", "item_ref", "item_b", "/items");

        final String message = ValidationErrorFormatter.format(error, model);

        // error location should be human-readable (display name, not raw index)
        assertFalse(message.contains("/0"), "Message must not contain raw index '/0': " + message);
        assertTrue(message.contains("item_a"), "Message must contain display name 'item_a': " + message);
        // structured fields should appear verbatim
        assertTrue(message.contains("item_ref"), "Message must contain referencingKey 'item_ref': " + message);
        assertTrue(message.contains("item_b"), "Message must contain objectKey 'item_b': " + message);
        // referenced object path stays raw
        assertTrue(message.contains("/items"), "Message must contain raw referencedObjectPath '/items': " + message);
    }

    @Test
    void emptyKey_messageContainsDisplayPath()
    {
        final ModelImpl model = buildModel();
        final ValidationError error = ValidationError.emptyKey("/items/0");

        final String message = ValidationErrorFormatter.format(error, model);

        assertFalse(message.contains("/0"), "Message must not contain raw index '/0': " + message);
        assertTrue(message.contains("item_a"), "Message must contain display name 'item_a': " + message);
        assertTrue(message.contains("Empty or missing reference key"), "Message must describe the error kind");
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
