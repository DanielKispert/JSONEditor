package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.daniel.jsoneditor.model.validation.ModelValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.commands.CommandManagerImpl;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.impl.SetValueAtNodeCommand;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that model mutation methods enforce schema validation and throw ModelValidationException for invalid data.
 * These tests should FAIL until validation is implemented in ModelImpl.
 */
public class ModelValidationTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private ModelImpl model;

    @BeforeEach
    void setup()
    {
        model = createModelWithStrictSchema();
    }

    @Nested
    class SetValueAtPathValidation
    {
        @Test
        void rejectsValueViolatingTypeConstraint()
        {
            // Schema says "age" is integer with minimum 0. Setting a string should be rejected.
            assertThrows(ModelValidationException.class, () -> model.setValueAtPath("/person", "age", "not_a_number"));
        }

        @Test
        void rejectsValueBelowMinimum()
        {
            // Schema says "age" has minimum 0. Setting -1 should be rejected.
            assertThrows(ModelValidationException.class, () -> model.setValueAtPath("/person", "age", -1));
        }

        @Test
        void acceptsValidValue()
        {
            // Schema says "age" is integer with minimum 0. Setting 25 should succeed.
            assertDoesNotThrow(() -> model.setValueAtPath("/person", "age", 25));
            assertEquals(25, model.getNodeForPath("/person/age").getNode().asInt());
        }

        @Test
        void acceptsRemovalOfOptionalProperty()
        {
            // "age" is not in "required", so removing it should succeed.
            assertDoesNotThrow(() -> model.setValueAtPath("/person", "age", null));
        }

        @Test
        void rejectsRemovalOfRequiredProperty()
        {
            // "name" is required. Removing it should be rejected.
            assertThrows(ModelValidationException.class, () -> model.setValueAtPath("/person", "name", null));
        }
    }

    @Nested
    class SetNodeValidation
    {
        @Test
        void rejectsNodeViolatingSchema()
        {
            // Replace /person with an object missing required "name"
            final ObjectNode invalidPerson = MAPPER.createObjectNode();
            invalidPerson.put("age", 30);
            // missing "name" which is required
            assertThrows(ModelValidationException.class, () -> model.setNode("/person", invalidPerson));
        }

        @Test
        void acceptsValidNode()
        {
            final ObjectNode validPerson = MAPPER.createObjectNode();
            validPerson.put("name", "Alice");
            validPerson.put("age", 30);
            assertDoesNotThrow(() -> model.setNode("/person", validPerson));
            assertEquals("Alice", model.getNodeForPath("/person/name").getNode().asText());
        }

        @Test
        void acceptsNullContentForRemoval()
        {
            // Setting null removes the node — should not trigger schema validation
            assertDoesNotThrow(() -> model.setNode("/person/age", null));
        }
    }

    @Nested
    class SetNodeFallbackSchemaHandling
    {
        /**
         * Setting a node at a path not declared in the schema (additional property) must succeed
         * because the editor should not reject additional properties when the schema doesn't forbid them.
         */
        @Test
        void acceptsNodeAtUndeclaredPath()
        {
            final ObjectNode extraData = MAPPER.createObjectNode();
            extraData.put("key", "value");
            // "/extra" is not in the schema properties, but no additionalProperties:false either
            assertDoesNotThrow(() -> model.setNode("/extra", extraData));
        }

        @Test
        void acceptsNodeAtRootPath()
        {
            // Setting at root (empty path) should skip validation entirely
            final ObjectNode newRoot = MAPPER.createObjectNode();
            final ObjectNode person = MAPPER.createObjectNode();
            person.put("name", "Charlie");
            person.put("age", 10);
            newRoot.set("person", person);
            assertDoesNotThrow(() -> model.setNode("", newRoot));
        }

        @Test
        void acceptsNullPathGracefully()
        {
            final ObjectNode node = MAPPER.createObjectNode();
            assertDoesNotThrow(() -> model.setNode(null, node));
        }
    }

    @Nested
    class SetValueAtPathEdgeCases
    {
        @Test
        void silentlyReturnsForNonExistentParent()
        {
            // Parent path doesn't exist — should return without throwing
            assertDoesNotThrow(() -> model.setValueAtPath("/nonexistent", "field", "value"));
        }

        @Test
        void silentlyReturnsForNonObjectParent()
        {
            // /person/name is a string, not an object — can't set properties on it
            assertDoesNotThrow(() -> model.setValueAtPath("/person/name", "sub", "value"));
        }

        @Test
        void validValueIsActuallyApplied()
        {
            model.setValueAtPath("/person", "name", "NewName");
            assertEquals("NewName", model.getNodeForPath("/person/name").getNode().asText());
        }

        @Test
        void rejectedValueDoesNotMutateModel()
        {
            final String originalName = model.getNodeForPath("/person/name").getNode().asText();
            try
            {
                // Try to set age to invalid string — should throw
                model.setValueAtPath("/person", "age", "not_a_number");
                fail("Expected ModelValidationException");
            }
            catch (ModelValidationException e)
            {
                // Model should remain unchanged
                assertEquals(originalName, model.getNodeForPath("/person/name").getNode().asText());
                assertEquals(42, model.getNodeForPath("/person/age").getNode().asInt());
            }
        }
    }

    @Nested
    class UndoRedoThroughValidation
    {
        @Test
        void undoOfValidChangeSucceeds()
        {
            final CommandManager manager = new CommandManagerImpl(model);
            // Valid change: set age from 42 to 25
            manager.executeCommand(new SetValueAtNodeCommand(model, "/person", "age", 25));
            assertEquals(25, model.getNodeForPath("/person/age").getNode().asInt());

            // Undo should restore 42 without validation blocking it
            manager.undo();
            assertEquals(42, model.getNodeForPath("/person/age").getNode().asInt());
        }

        @Test
        void redoOfValidChangeSucceeds()
        {
            final CommandManager manager = new CommandManagerImpl(model);
            manager.executeCommand(new SetValueAtNodeCommand(model, "/person", "age", 25));
            manager.undo();
            assertEquals(42, model.getNodeForPath("/person/age").getNode().asInt());

            // Redo should set back to 25
            manager.redo();
            assertEquals(25, model.getNodeForPath("/person/age").getNode().asInt());
        }

        @Test
        void invalidCommandThrowsAndDoesNotPushToUndoStack()
        {
            final CommandManager manager = new CommandManagerImpl(model);
            try
            {
                manager.executeCommand(new SetValueAtNodeCommand(model, "/person", "age", "invalid"));
                fail("Expected ModelValidationException");
            }
            catch (ModelValidationException e)
            {
                // Exception propagates from model through command execution
            }
            // Undo stack should be empty — the invalid command was never recorded
            final List<ModelChange> undoResult = manager.undo();
            assertTrue(undoResult.isEmpty());
            // Model unchanged
            assertEquals(42, model.getNodeForPath("/person/age").getNode().asInt());
        }
    }

    @Nested
    class ComplexSchemaValidation
    {
        /**
         * Tests validation with more complex schema structures: arrays of typed objects,
         * required fields, and numeric constraints — typical patterns for real-world JSON schemas.
         */
        @Test
        void rejectsProcessWithMissingRequiredId()
        {
            final ModelImpl m = createModelWithArraySchema();
            // Try to set a process node without the required "id" field
            final ObjectNode invalidProcess = MAPPER.createObjectNode();
            invalidProcess.put("name", "SomeProcess");
            // missing "id" which is required
            assertThrows(ModelValidationException.class, () -> m.setNode("/processes/0", invalidProcess));
        }

        @Test
        void acceptsValidProcess()
        {
            final ModelImpl m = createModelWithArraySchema();
            final ObjectNode validProcess = MAPPER.createObjectNode();
            validProcess.put("id", "new_process");
            assertDoesNotThrow(() -> m.setNode("/processes/0", validProcess));
            assertEquals("new_process", m.getNodeForPath("/processes/0/id").getNode().asText());
        }

        @Test
        void rejectsWrongTypeForProcessId()
        {
            final ModelImpl m = createModelWithArraySchema();
            // id must be string, setting integer should fail
            assertThrows(ModelValidationException.class,
                () -> m.setValueAtPath("/processes/0", "id", 12345));
        }

        @Test
        void acceptsValidStringForProcessId()
        {
            final ModelImpl m = createModelWithArraySchema();
            assertDoesNotThrow(() -> m.setValueAtPath("/processes/0", "id", "renamed_process"));
            assertEquals("renamed_process", m.getNodeForPath("/processes/0/id").getNode().asText());
        }

        @Test
        void rejectsNegativeSortOrder()
        {
            final ModelImpl m = createModelWithArraySchema();
            // sortOrder has minimum: 0
            assertThrows(ModelValidationException.class,
                () -> m.setValueAtPath("/processes/0", "sortOrder", -1));
        }

        @Test
        void acceptsValidSortOrder()
        {
            final ModelImpl m = createModelWithArraySchema();
            assertDoesNotThrow(() -> m.setValueAtPath("/processes/0", "sortOrder", 5));
            assertEquals(5, m.getNodeForPath("/processes/0/sortOrder").getNode().asInt());
        }

        @Test
        void referenceKeyRenamingWorksWithSchemaDefinedKey()
        {
            final ModelImpl m = createModelWithArraySchema();
            final List<ReferenceableObject> objects = m.getReferenceableObjects();
            assertFalse(objects.isEmpty(), "Schema should declare referenceableObjects");
            final ReferenceableObject processRef = objects.get(0);
            assertEquals("/id", processRef.getKey());

            // Rename key of first process — exercises ReferenceHelper.setKeyOfInstance → setKeyNode
            ReferenceHelper.setKeyOfInstance(m, processRef, "/processes/0", "renamed_id");
            assertEquals("renamed_id", m.getNodeForPath("/processes/0/id").getNode().asText());
        }

        @Test
        void referenceKeyRenamingHandlesUndefinedSchemaPath()
        {
            // Test with a path where getSubschemaForPath returns null (key path not in schema)
            final ModelImpl m = createModelWithArraySchema();
            final ReferenceableObject objectWithBadKey = new ReferenceableObject("bad_ref", "/processes", "/nonexistent");

            // Should not throw NPE — the null guard in ReferenceHelper should handle this
            assertDoesNotThrow(() -> ReferenceHelper.setKeyOfInstance(m, objectWithBadKey, "/processes/0", "value"));
        }

        /**
         * Creates a model with a complex schema: root object containing an array of typed objects
         * with required string id and optional integer sortOrder (minimum 0).
         */
        private ModelImpl createModelWithArraySchema()
        {
            final ObjectNode schemaRoot = MAPPER.createObjectNode();
            schemaRoot.put("type", "object");

            // Process item schema (simulates resolved $defs/process)
            final ObjectNode processItemSchema = MAPPER.createObjectNode();
            processItemSchema.put("type", "object");
            final ObjectNode processProps = MAPPER.createObjectNode();
            processProps.set("id", MAPPER.createObjectNode().put("type", "string"));
            final ObjectNode sortOrderSchema = MAPPER.createObjectNode();
            sortOrderSchema.put("type", "integer");
            sortOrderSchema.put("minimum", 0);
            processProps.set("sortOrder", sortOrderSchema);
            processItemSchema.set("properties", processProps);
            processItemSchema.set("required", MAPPER.createArrayNode().add("id"));

            // Processes array
            final ObjectNode processesSchema = MAPPER.createObjectNode();
            processesSchema.put("type", "array");
            processesSchema.set("items", processItemSchema);

            final ObjectNode rootProperties = MAPPER.createObjectNode();
            rootProperties.set("processes", processesSchema);
            schemaRoot.set("properties", rootProperties);

            // Add referenceableObjects declaration (exercises ReferenceHelper code paths)
            final ArrayNode referenceableObjects = MAPPER.createArrayNode();
            final ObjectNode processRefDef = MAPPER.createObjectNode();
            processRefDef.put("referencingKey", "item_ref");
            processRefDef.put("path", "/processes");
            processRefDef.put("key", "/id");
            referenceableObjects.add(processRefDef);
            schemaRoot.set("referenceableObjects", referenceableObjects);

            schemaRoot.set("required", MAPPER.createArrayNode().add("processes"));

            final JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaRoot);

            // Data: one process with valid id
            final ObjectNode data = MAPPER.createObjectNode();
            final ArrayNode processes = MAPPER.createArrayNode();
            final ObjectNode process = MAPPER.createObjectNode();
            process.put("id", "existing_process");
            process.put("sortOrder", 1);
            processes.add(process);
            data.set("processes", processes);

            final ModelImpl m = new ModelImpl(new EventSenderImpl());
            m.jsonAndSchemaSuccessfullyValidated(
                new File("dummy.json"), new File("dummy_schema.json"), data, schema);
            return m;
        }
    }

    /**
     * Creates a model with a strict schema that has type constraints, required fields, and minimum values.
     * Schema structure:
     * {
     *   "type": "object",
     *   "properties": {
     *     "person": {
     *       "type": "object",
     *       "properties": {
     *         "name": { "type": "string" },
     *         "age": { "type": "integer", "minimum": 0 }
     *       },
     *       "required": ["name"]
     *     }
     *   },
     *   "required": ["person"]
     * }
     */
    private ModelImpl createModelWithStrictSchema()
    {
        final ObjectNode schemaRoot = MAPPER.createObjectNode();
        schemaRoot.put("type", "object");

        final ObjectNode personSchema = MAPPER.createObjectNode();
        personSchema.put("type", "object");
        final ObjectNode personProperties = MAPPER.createObjectNode();
        personProperties.set("name", MAPPER.createObjectNode().put("type", "string"));
        final ObjectNode ageSchema = MAPPER.createObjectNode();
        ageSchema.put("type", "integer");
        ageSchema.put("minimum", 0);
        personProperties.set("age", ageSchema);
        personSchema.set("properties", personProperties);
        personSchema.set("required", MAPPER.createArrayNode().add("name"));

        final ObjectNode rootProperties = MAPPER.createObjectNode();
        rootProperties.set("person", personSchema);
        schemaRoot.set("properties", rootProperties);
        schemaRoot.set("required", MAPPER.createArrayNode().add("person"));

        final JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaRoot);

        final ObjectNode data = MAPPER.createObjectNode();
        final ObjectNode person = MAPPER.createObjectNode();
        person.put("name", "Bob");
        person.put("age", 42);
        data.set("person", person);

        final ModelImpl m = new ModelImpl(new EventSenderImpl());
        m.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), data, schema);
        return m;
    }
}
