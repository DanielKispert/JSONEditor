package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.daniel.jsoneditor.model.statemachine.impl.StateMachineImpl;
import com.fasterxml.jackson.databind.JsonNode;
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
 * Baseline Tests zur aktuellen ModelImpl Funktionalität vor Refactor (ModelChange/EventBus).
 */
public class ModelImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Baut ein Modell mit Root-JSON und passendem Schema für Arrays: /arr (object items), /strings (string items), /numbers (number items).
     */
    private ModelImpl createModel() {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.set("arr", MAPPER.createArrayNode());
            ArrayNode strings = MAPPER.createArrayNode();
            strings.add("delta");
            strings.add("alpha");
            strings.add("beta");
            root.set("strings", strings);
            ArrayNode numbers = MAPPER.createArrayNode();
            numbers.add(5);
            numbers.add(2);
            numbers.add(10);
            root.set("numbers", numbers);

            // Schema
            ObjectNode schemaRoot = MAPPER.createObjectNode();
            schemaRoot.put("type", "object");
            ObjectNode properties = MAPPER.createObjectNode();
            // arr
            ObjectNode arrSchema = MAPPER.createObjectNode();
            arrSchema.put("type", "array");
            ObjectNode arrItems = MAPPER.createObjectNode();
            arrItems.put("type", "object");
            ObjectNode arrItemProps = MAPPER.createObjectNode();
            ObjectNode nameProp = MAPPER.createObjectNode();
            nameProp.put("type", "string");
            arrItemProps.set("name", nameProp);
            arrItems.set("properties", arrItemProps);
            properties.set("arr", arrSchema);
            arrSchema.set("items", arrItems);
            // strings
            ObjectNode stringsSchema = MAPPER.createObjectNode();
            stringsSchema.put("type", "array");
            ObjectNode stringItems = MAPPER.createObjectNode();
            stringItems.put("type", "string");
            stringsSchema.set("items", stringItems);
            properties.set("strings", stringsSchema);
            // numbers
            ObjectNode numbersSchema = MAPPER.createObjectNode();
            numbersSchema.put("type", "array");
            ObjectNode numberItems = MAPPER.createObjectNode();
            numberItems.put("type", "number");
            numbersSchema.set("items", numberItems);
            properties.set("numbers", numbersSchema);
            schemaRoot.set("properties", properties);

            JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaRoot);

            ModelImpl model = new ModelImpl(new StateMachineImpl());
            model.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), root, schema);
            assertEquals(EventEnum.MAIN_EDITOR, model.getLatestEvent().getEvent(), "Initial Event nach Validation");
            return model;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAddNodeToArray() {
        ModelImpl model = createModel();
        model.addNodeToArray("/arr");
        assertEquals(EventEnum.ADDED_ITEM_TO_ARRAY_FROM_ARRAY, model.getLatestEvent().getEvent());
        JsonNode arr = model.getNodeForPath("/arr").getNode();
        assertEquals(1, arr.size());
    }

    @Test
    void testSetAndRemoveNode() {
        ModelImpl model = createModel();
        ObjectNode obj = MAPPER.createObjectNode();
        obj.put("name", "First");
        model.setNode("/arr/0", obj);
        assertEquals(EventEnum.UPDATED_JSON_STRUCTURE, model.getLatestEvent().getEvent());
        assertEquals("First", model.getNodeForPath("/arr/0").getNode().get("name").asText());
        model.removeNode("/arr/0");
        assertEquals(EventEnum.REMOVED_SELECTED_JSON_NODE, model.getLatestEvent().getEvent());
        assertEquals(0, model.getNodeForPath("/arr").getNode().size());
    }

    @Test
    void testMoveItemToIndex() {
        ModelImpl model = createModel();
        // zwei Items erzeugen
        ObjectNode first = MAPPER.createObjectNode(); first.put("name", "First");
        ObjectNode second = MAPPER.createObjectNode(); second.put("name", "Second");
        model.setNode("/arr/0", first);
        model.setNode("/arr/1", second);
        JsonNodeWithPath secondPath = model.getNodeForPath("/arr/1");
        model.moveItemToIndex(secondPath, 0);
        assertEquals(EventEnum.MOVED_CHILD_OF_SELECTED_JSON_NODE, model.getLatestEvent().getEvent());
        assertEquals("Second", model.getNodeForPath("/arr/0").getNode().get("name").asText());
    }

    @Test
    void testDuplicateArrayItem() {
        ModelImpl model = createModel();
        ObjectNode first = MAPPER.createObjectNode(); first.put("name", "Orig");
        model.setNode("/arr/0", first);
        int before = model.getNodeForPath("/arr").getNode().size();
        model.duplicateArrayItem("/arr/0");
        assertEquals(EventEnum.UPDATED_JSON_STRUCTURE, model.getLatestEvent().getEvent());
        JsonNode arr = model.getNodeForPath("/arr").getNode();
        assertEquals(before + 1, arr.size());
        assertEquals("Orig", arr.get(0).get("name").asText());
        assertEquals("Orig", arr.get(1).get("name").asText());
    }

    @Test
    void testSortArrayStrings() {
        ModelImpl model = createModel();
        model.sortArray("/strings");
        assertEquals(EventEnum.UPDATED_JSON_STRUCTURE, model.getLatestEvent().getEvent());
        ArrayNode strings = (ArrayNode) model.getNodeForPath("/strings").getNode();
        assertEquals("alpha", strings.get(0).asText());
        assertEquals("beta", strings.get(1).asText());
        assertEquals("delta", strings.get(2).asText());
    }

    @Test
    void testSortArrayNumbers() {
        ModelImpl model = createModel();
        model.sortArray("/numbers");
        assertEquals(EventEnum.UPDATED_JSON_STRUCTURE, model.getLatestEvent().getEvent());
        ArrayNode numbers = (ArrayNode) model.getNodeForPath("/numbers").getNode();
        assertEquals(2, numbers.get(0).asInt());
        assertEquals(5, numbers.get(1).asInt());
        assertEquals(10, numbers.get(2).asInt());
    }

    @Test
    void testJsonAndSchemaValidationSetsMainEditor() {
        ModelImpl model = createModel();
        assertEquals(EventEnum.MAIN_EDITOR, model.getLatestEvent().getEvent());
    }
}

