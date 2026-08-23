package com.daniel.jsoneditor.model.validation;

import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.validation.ValidationError;
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
 * Verifies that ReferenceValidator correctly detects dangling cross-object references.
 */
public class ReferenceValidatorTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    // Schema: /items array referenceable by "item_ref" (keyed by /id).
    // /items/*/links entries reference items via /ref_type + /ref_id.
    private static final String SCHEMA_JSON = "{\"type\":\"object\","
            + "\"properties\":{\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"id\":{\"type\":\"string\"},"
            + "\"links\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"ref_type\":{\"type\":\"string\"},"
            + "\"ref_id\":{\"type\":\"string\"},\"label\":{\"type\":\"string\"}}}}}}}},"
            + "\"referenceableObjects\":[{\"referencingKey\":\"item_ref\",\"path\":\"/items\",\"key\":\"/id\"}],"
            + "\"referencesToObjects\":[{\"path\":\"/items/*/links\","
            + "\"objectReferencingKey\":\"/ref_type\","
            + "\"objectKey\":\"/ref_id\","
            + "\"referenceRemarks\":\"/label\"}]}";

    @Test
    void danglingReference_isStructured_withRawPathAndKindAndKeys()
    {
        // item_a exists; its links point to item_b which does NOT exist -> dangling reference.
        final ModelImpl model = buildModel(buildItemWithLink("item_a", "item_b", "item_ref", "lbl_1"));

        final ValidationResult result = ReferenceValidator.validateReferences(model);

        assertFalse(result.isValid(), "Dangling reference should be flagged as invalid");
        assertEquals(1, result.getErrorCount(), "Expected exactly one validation error");
        final ValidationError error = result.getErrors().get(0);

        // structured type
        assertEquals(ValidationError.Type.DANGLING_REFERENCE, error.getType());

        // error location: raw JSON pointer (not display name)
        assertTrue(error.getPath().contains("/items"), "Path must contain '/items': " + error.getPath());
        assertTrue(error.getPath().contains("links"), "Path must contain 'links': " + error.getPath());
        assertFalse(error.getPath().contains("item_a"), "Path must not contain display name: " + error.getPath());

        // structured fields for view-layer formatting
        assertEquals("item_ref", error.getReferencingKey(), "referencingKey should be the schema reference type");
        assertEquals("item_b", error.getObjectKey(), "objectKey should be the unresolvable target key");
        assertEquals("/items", error.getReferencedObjectPath(), "referencedObjectPath should be the raw array path");
    }

    @Test
    void validReferencePassesValidation()
    {
        // item_a references item_b; item_b also exists -> valid reference.
        final ObjectNode data = MAPPER.createObjectNode();
        final ArrayNode items = MAPPER.createArrayNode();

        final ObjectNode itemA = MAPPER.createObjectNode();
        itemA.put("id", "item_a");
        final ArrayNode links = MAPPER.createArrayNode();
        final ObjectNode link = MAPPER.createObjectNode();
        link.put("ref_type", "item_ref");
        link.put("ref_id", "item_b");
        link.put("label", "lbl_1");
        links.add(link);
        itemA.set("links", links);

        final ObjectNode itemB = MAPPER.createObjectNode();
        itemB.put("id", "item_b");
        itemB.set("links", MAPPER.createArrayNode());

        items.add(itemA);
        items.add(itemB);
        data.set("items", items);

        final ValidationResult result = ReferenceValidator.validateReferences(buildModel(data));

        assertTrue(result.isValid(), "Valid cross-reference should pass; errors: " + result.getErrorSummary());
    }

    // --- helpers ---

    private static ObjectNode buildItemWithLink(
            final String itemId, final String targetId, final String refType, final String label)
    {
        final ObjectNode data = MAPPER.createObjectNode();
        final ArrayNode items = MAPPER.createArrayNode();
        final ObjectNode item = MAPPER.createObjectNode();
        item.put("id", itemId);
        final ArrayNode links = MAPPER.createArrayNode();
        final ObjectNode link = MAPPER.createObjectNode();
        link.put("ref_type", refType);
        link.put("ref_id", targetId);
        link.put("label", label);
        links.add(link);
        item.set("links", links);
        items.add(item);
        data.set("items", items);
        return data;
    }

    private static ModelImpl buildModel(final ObjectNode data)
    {
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
