package com.daniel.jsoneditor.controller.commands;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.commands.CommandFactory;
import com.daniel.jsoneditor.model.changes.ChangeType;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.statemachine.impl.StateMachineImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für Undo/Redo über neuen ModelChange-basierten CommandManager.
 */
public class CommandManagerTest {
    private static final ObjectMapper M = new ObjectMapper();

    private ModelImpl createModel() throws Exception {
        ObjectNode root = M.createObjectNode();
        root.set("arr", M.createArrayNode());
        ObjectNode schemaRoot = M.createObjectNode();
        schemaRoot.put("type", "object");
        ObjectNode properties = M.createObjectNode();
        ObjectNode arrSchema = M.createObjectNode();
        arrSchema.put("type", "array");
        ObjectNode items = M.createObjectNode();
        items.put("type", "object");
        items.set("properties", M.createObjectNode());
        arrSchema.set("items", items);
        properties.set("arr", arrSchema);
        schemaRoot.set("properties", properties);
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaRoot);
        ModelImpl model = new ModelImpl(new StateMachineImpl());
        model.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), root, schema);
        return model;
    }

    @Test
    void testExecuteUndoRedoSingleAdd() throws Exception {
        ModelImpl model = createModel();
        CommandManager mgr = new CommandManager(model);
        CommandFactory factory = model.getCommandFactory();

        List<ModelChange> changes = mgr.executeCommand(factory.addNodeToArrayCommand("/arr"));
        assertEquals(1, changes.size());
        ModelChange c = changes.get(0);
        assertEquals(ChangeType.ADD, c.type());
        assertEquals("/arr/0", c.path());
        assertNotNull(c.newValue());
        ArrayNode arr = (ArrayNode) model.getNodeForPath("/arr").getNode();
        assertEquals(1, arr.size());

        List<ModelChange> undo = mgr.undo();
        assertEquals(1, undo.size());
        ModelChange u = undo.get(0);
        assertEquals(ChangeType.REMOVE, u.type());
        assertEquals("/arr/0", u.path());
        assertEquals(0, arr.size());

        List<ModelChange> redo = mgr.redo();
        assertEquals(1, redo.size());
        ModelChange r = redo.get(0);
        assertEquals(ChangeType.ADD, r.type());
        assertEquals(1, arr.size());
    }

    @Test
    void testUndoEmptyStack() throws Exception {
        ModelImpl model = createModel();
        CommandManager mgr = new CommandManager(model);
        assertTrue(mgr.undo().isEmpty());
        assertTrue(mgr.redo().isEmpty());
    }

    @Test
    void testMultipleAddsUndoRedoOrder() throws Exception {
        ModelImpl model = createModel();
        CommandManager mgr = new CommandManager(model);
        CommandFactory factory = model.getCommandFactory();
        mgr.executeCommand(factory.addNodeToArrayCommand("/arr"));
        mgr.executeCommand(factory.addNodeToArrayCommand("/arr"));
        mgr.executeCommand(factory.addNodeToArrayCommand("/arr"));
        ArrayNode arr = (ArrayNode) model.getNodeForPath("/arr").getNode();
        assertEquals(3, arr.size());
        mgr.undo(); // remove third
        assertEquals(2, arr.size());
        mgr.undo(); // remove second
        assertEquals(1, arr.size());
        mgr.redo(); // add second back
        assertEquals(2, arr.size());
        mgr.redo(); // add third back
        assertEquals(3, arr.size());
    }
}

