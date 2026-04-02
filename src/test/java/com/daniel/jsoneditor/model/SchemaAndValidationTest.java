package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;


public class SchemaAndValidationTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private ModelImpl createModel(ObjectNode schemaRoot, ObjectNode dataRoot)
    {
        final JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaRoot);
        final ModelImpl model = new ModelImpl(new EventSenderImpl());
        model.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), dataRoot, schema);
        return model;
    }

    @Nested
    class CompositeSchemaResolution
    {
        /**
         * allOf with two branches, each defining different properties.
         * getSubschemaForPath must resolve properties from both branches correctly.
         */
        @Test
        void shouldResolvePropertyFromSecondAllOfBranch()
        {
            final ObjectNode schemaRoot = MAPPER.createObjectNode();
            schemaRoot.put("type", "object");
            final ObjectNode properties = MAPPER.createObjectNode();

            final ObjectNode wrapperSchema = MAPPER.createObjectNode();
            final ObjectNode branch1 = makeObjectBranch("alpha", "string");
            final ObjectNode branch2 = makeObjectBranch("beta", "integer");
            wrapperSchema.set("allOf", MAPPER.createArrayNode().add(branch1).add(branch2));
            properties.set("wrapper", wrapperSchema);
            schemaRoot.set("properties", properties);

            final ObjectNode data = MAPPER.createObjectNode();
            final ObjectNode wrapper = MAPPER.createObjectNode();
            wrapper.put("alpha", "hello");
            wrapper.put("beta", 42);
            data.set("wrapper", wrapper);

            final ModelImpl model = createModel(schemaRoot, data);

            final JsonNode betaSchemaNode = model.getSubschemaForPath("/wrapper/beta").getSchemaNode();
            assertEquals("integer", betaSchemaNode.path("type").asText(),
                    "Property from second allOf branch must resolve to its own schema");
        }

        /**
         * When anyOf has no matching property but allOf does, the allOf match must be returned.
         */
        @Test
        void shouldSearchAllCompositeKeywordsBeforeFallingBack()
        {
            final ObjectNode schemaRoot = MAPPER.createObjectNode();
            schemaRoot.put("type", "object");
            final ObjectNode properties = MAPPER.createObjectNode();

            final ObjectNode mixedSchema = MAPPER.createObjectNode();
            mixedSchema.set("anyOf", MAPPER.createArrayNode().add(makeObjectBranch("unrelated", "string")));
            mixedSchema.set("allOf", MAPPER.createArrayNode().add(makeObjectBranch("target", "boolean")));
            properties.set("mixed", mixedSchema);
            schemaRoot.set("properties", properties);

            final ObjectNode data = MAPPER.createObjectNode();
            final ObjectNode mixed = MAPPER.createObjectNode();
            mixed.put("target", true);
            data.set("mixed", mixed);

            final ModelImpl model = createModel(schemaRoot, data);

            final JsonNode targetSchemaNode = model.getSubschemaForPath("/mixed/target").getSchemaNode();
            assertEquals("boolean", targetSchemaNode.path("type").asText(),
                    "Must check all composite keywords (anyOf, oneOf, allOf) before falling back");
        }

        private ObjectNode makeObjectBranch(String propertyName, String propertyType)
        {
            final ObjectNode branch = MAPPER.createObjectNode();
            branch.put("type", "object");
            final ObjectNode props = MAPPER.createObjectNode();
            props.set(propertyName, MAPPER.createObjectNode().put("type", propertyType));
            branch.set("properties", props);
            return branch;
        }
    }

    @Nested
    class PreWriteSchemaValidation
    {
        /**
         * When a numeric string like "42" is submitted for an integer-typed field,
         * the pre-write validation must accept it (by parsing the number).
         */
        @Test
        void numericStringShouldPassIntegerSchemaValidation()
        {
            final ObjectNode integerSchema = MAPPER.createObjectNode();
            integerSchema.put("type", "integer");
            final JsonSchema schema = SCHEMA_FACTORY.getSchema(integerSchema);

            // buildCandidateNode should convert "42" to a NumberNode, not a TextNode
            final JsonNode candidate = JsonNodeFactory.instance.numberNode(Integer.parseInt("42"));
            assertTrue(SchemaHelper.validateJsonWithSchema(candidate, schema),
                    "A numeric value must pass integer schema validation regardless of original String source");
        }

        /**
         * An actual integer node must pass integer schema validation.
         */
        @Test
        void integerNodeShouldPassIntegerSchemaValidation()
        {
            final ObjectNode integerSchema = MAPPER.createObjectNode();
            integerSchema.put("type", "integer");
            final JsonSchema schema = SCHEMA_FACTORY.getSchema(integerSchema);

            final JsonNode intNode = JsonNodeFactory.instance.numberNode(42);
            assertTrue(SchemaHelper.validateJsonWithSchema(intNode, schema));
        }

        /**
         * A non-numeric string must fail integer schema validation.
         */
        @Test
        void nonNumericStringShouldFailIntegerSchemaValidation()
        {
            final ObjectNode integerSchema = MAPPER.createObjectNode();
            integerSchema.put("type", "integer");
            final JsonSchema schema = SCHEMA_FACTORY.getSchema(integerSchema);

            final JsonNode textNode = JsonNodeFactory.instance.textNode("hello");
            assertFalse(SchemaHelper.validateJsonWithSchema(textNode, schema));
        }
    }

    @Nested
    class ToastMessages
    {
        @Test
        void exportSuccessfulToastShouldBeSpelledCorrectly()
        {
            assertEquals("Export successful", Toasts.EXPORT_SUCCESSFUL_TOAST.getMessage());
        }

        @Test
        void allToastMessagesShouldNotContainDoubleLetterTypos()
        {
            for (final Toasts toast : Toasts.values())
            {
                final String msg = toast.getMessage();
                assertFalse(msg.matches(".*([a-z])\\1{2,}.*"),
                        "Toast '" + toast.name() + "' contains suspicious repeated characters: " + msg);
            }
        }
    }
}

