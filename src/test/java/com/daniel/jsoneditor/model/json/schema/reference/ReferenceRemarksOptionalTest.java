package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves that a referencesToObjects schema entry without the optional referenceRemarks field
 * is still parsed by ReferenceHelper — i.e. referenceRemarks must be treated as optional.
 */
public class ReferenceRemarksOptionalTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    // Schema: /objects referenceable by "obj_ref" (keyed by /name).
    // /refs entries point at objects via /type + /target — intentionally NO referenceRemarks.
    private static final String SCHEMA_JSON = "{\"type\":\"object\","
            + "\"properties\":{"
            + "\"objects\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"name\":{\"type\":\"string\"}}}},"
            + "\"refs\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"type\":{\"type\":\"string\"},\"target\":{\"type\":\"string\"}}}}},"
            + "\"referenceableObjects\":[{\"referencingKey\":\"obj_ref\",\"path\":\"/objects\",\"key\":\"/name\"}],"
            + "\"referencesToObjects\":[{\"path\":\"/refs\","
            + "\"objectReferencingKey\":\"/type\","
             + "\"objectKey\":\"/target\"}]}";

    // Same shape as SCHEMA_JSON but refs carry a "label" property and
    // referencesToObjects declares referenceRemarks pointing at /label.
    private static final String SCHEMA_WITH_REMARKS_JSON = "{\"type\":\"object\","
            + "\"properties\":{"
            + "\"objects\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"name\":{\"type\":\"string\"}}}},"
            + "\"refs\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"type\":{\"type\":\"string\"},\"target\":{\"type\":\"string\"},"
            + "\"label\":{\"type\":\"string\"}}}}},"
            + "\"referenceableObjects\":[{\"referencingKey\":\"obj_ref\",\"path\":\"/objects\",\"key\":\"/name\"}],"
            + "\"referencesToObjects\":[{\"path\":\"/refs\","
            + "\"objectReferencingKey\":\"/type\","
            + "\"objectKey\":\"/target\","
            + "\"referenceRemarks\":\"/label\"}]}";

    @Test
    void referenceWithoutRemarksIsStillParsed()
    {
        final ObjectNode data = MAPPER.createObjectNode();
        final ArrayNode objects = MAPPER.createArrayNode();
        final ObjectNode obj = MAPPER.createObjectNode();
        obj.put("name", "alpha");
        objects.add(obj);
        data.set("objects", objects);
        final ArrayNode refs = MAPPER.createArrayNode();
        final ObjectNode ref = MAPPER.createObjectNode();
        ref.put("type", "obj_ref");
        ref.put("target", "alpha");
        refs.add(ref);
        data.set("refs", refs);

        final ModelImpl model = buildModel(data);

        assertNotNull(model.getReferenceToObject("/refs/0"),
                "getReferenceToObject must not return null when referenceRemarks is absent from the schema definition");
    }

    @Test
    void referenceWithRemarksIsParsedAndRemarksFlowThrough()
    {
        final ObjectNode data = MAPPER.createObjectNode();
        final ArrayNode objects = MAPPER.createArrayNode();
        final ObjectNode obj = MAPPER.createObjectNode();
        obj.put("name", "alpha");
        objects.add(obj);
        data.set("objects", objects);
        final ArrayNode refs = MAPPER.createArrayNode();
        final ObjectNode ref = MAPPER.createObjectNode();
        ref.put("type", "obj_ref");
        ref.put("target", "alpha");
        ref.put("label", "my remark");
        refs.add(ref);
        data.set("refs", refs);

        final ModelImpl model = buildModel(data, SCHEMA_WITH_REMARKS_JSON);
        final ReferenceToObject refToObj = model.getReferenceToObject("/refs/0");
        final JsonNode refInstanceNode = data.at("/refs/0");

        assertNotNull(refToObj,
                "getReferenceToObject must not return null when referenceRemarks is present in schema");
        assertEquals("my remark", refToObj.getRemarksOfInstance(refInstanceNode),
                "remarks value must flow through from instance node via the schema-declared path");
    }

    // --- helpers ---

    private static ModelImpl buildModel(final ObjectNode data, final String schemaJson)
    {
        final JsonSchema schema;
        try
        {
            schema = SCHEMA_FACTORY.getSchema(MAPPER.readTree(schemaJson));
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

    private static ModelImpl buildModel(final ObjectNode data)
    {
        return buildModel(data, SCHEMA_JSON);
    }
}
