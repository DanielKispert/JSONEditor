package com.daniel.jsoneditor.model.commands;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.model.changes.ChangeType;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.impl.AddNodeToArrayCommand;
import com.daniel.jsoneditor.model.commands.impl.SetValueAtNodeCommand;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.StateMachineImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests core command semantics: add/replace/remove cycles, no-op changes, array insert sequencing and failure cases.
 */
public class ModelCommandsTest {

    private static final ObjectMapper M = new ObjectMapper();

    private ModelImpl model;
    private CommandManager manager;

    @BeforeEach
    void setup() throws Exception {
        model = createModel();
        manager = new CommandManager(model);
    }

    /**
     * Verifies add -> replace -> remove cycle produces correct ModelChange types and undo/redo restores state.
     */
    @Test
    void testAddReplaceRemoveCycle() {
        // add (null -> value)
        List<ModelChange> add = manager.executeCommand(new SetValueAtNodeCommand(model, "", "a", "one"));
        assertSingle(add, ChangeType.ADD, "/a");
        assertEquals("one", model.getNodeForPath("/a").getNode().asText());

        // replace (one -> two)
        List<ModelChange> replace = manager.executeCommand(new SetValueAtNodeCommand(model, "", "a", "two"));
        assertSingle(replace, ChangeType.REPLACE, "/a");
        assertEquals("two", model.getNodeForPath("/a").getNode().asText());

        // remove (two -> null)
        List<ModelChange> remove = manager.executeCommand(new SetValueAtNodeCommand(model, "", "a", null));
        assertSingle(remove, ChangeType.REMOVE, "/a");
        assertTrue(model.getNodeForPath("/a").getNode().isMissingNode());

        // undo remove -> value restored
        manager.undo();
        assertEquals("two", model.getNodeForPath("/a").getNode().asText());

        // undo replace -> one
        manager.undo();
        assertEquals("one", model.getNodeForPath("/a").getNode().asText());

        // undo add -> missing
        manager.undo();
        assertTrue(model.getNodeForPath("/a").getNode().isMissingNode());

        // redo add
        manager.redo();
        assertEquals("one", model.getNodeForPath("/a").getNode().asText());
    }

    /**
     * Setting same value again should produce no changes and not push onto undo stack.
     */
    @Test
    void testNoChangeProducesNoUndoEntry() {
        manager.executeCommand(new SetValueAtNodeCommand(model, "", "a", "v"));
        // Same value again
        List<ModelChange> none = manager.executeCommand(new SetValueAtNodeCommand(model, "", "a", "v"));
        assertTrue(none.isEmpty());
        // Only one undo entry should exist; undo returns inverse (REMOVE of previously added)
        assertEquals(ChangeType.REMOVE, manager.undo().get(0).getType());
        assertTrue(manager.undo().isEmpty()); // undo stack empty now
    }

    /**
     * Appending multiple array elements yields sequential indices and undo removes last first.
     */
    @Test
    void testAddNodeToArraySequentialIndices() {
        List<ModelChange> c1 = manager.executeCommand(new AddNodeToArrayCommand(model, "/arr"));
        assertSingle(c1, ChangeType.ADD, "/arr/0");
        List<ModelChange> c2 = manager.executeCommand(new AddNodeToArrayCommand(model, "/arr"));
        assertSingle(c2, ChangeType.ADD, "/arr/1");
        List<ModelChange> c3 = manager.executeCommand(new AddNodeToArrayCommand(model, "/arr"));
        assertSingle(c3, ChangeType.ADD, "/arr/2");
        ArrayNode arr = (ArrayNode) model.getNodeForPath("/arr").getNode();
        assertEquals(3, arr.size());
        manager.undo();
        assertEquals(2, arr.size());
        manager.undo();
        assertEquals(1, arr.size());
        manager.redo();
        assertEquals(2, arr.size());
    }

    /**
     * Adding to non-array path should produce no ModelChange.
     */
    @Test
    void testAddNodeToArrayFailsForNonArray() {
        // root has no '/notArray' so create object at root first
        manager.executeCommand(new SetValueAtNodeCommand(model, "", "notArray", "value"));
        List<ModelChange> result = manager.executeCommand(new AddNodeToArrayCommand(model, "/notArray"));
        assertTrue(result.isEmpty());
    }

    private void assertSingle(List<ModelChange> changes, ChangeType type, String path) {
        assertEquals(1, changes.size());
        ModelChange c = changes.get(0);
        assertEquals(type, c.getType());
        assertEquals(path, c.getPath());
    }

    private ModelImpl createModel() throws Exception {
        ObjectNode root = M.createObjectNode();
        // root object fields as needed
        root.set("arr", M.createArrayNode());
        ObjectNode schemaRoot = M.createObjectNode();
        schemaRoot.put("type", "object");
        ObjectNode properties = M.createObjectNode();
        // schema for arr (array of empty objects)
        ObjectNode arrSchema = M.createObjectNode();
        arrSchema.put("type", "array");
        ObjectNode items = M.createObjectNode();
        items.put("type", "object");
        items.set("properties", M.createObjectNode());
        arrSchema.set("items", items);
        properties.set("arr", arrSchema);
        // allow arbitrary properties (like 'a', 'notArray')
        schemaRoot.set("properties", properties);
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaRoot);
        ModelImpl m = new ModelImpl(new StateMachineImpl());
        m.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), root, schema);
        return m;
    }
}
