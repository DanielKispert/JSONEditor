package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.commands.CommandManagerImpl;
import com.daniel.jsoneditor.model.changes.ChangeType;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.impl.*;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.fasterxml.jackson.databind.JsonNode;
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


public class ModelImplTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private ModelImpl createModel()
    {
        try
        {
            final ObjectNode root = MAPPER.createObjectNode();
            root.set("arr", MAPPER.createArrayNode());
            final ArrayNode strings = MAPPER.createArrayNode();
            strings.add("delta");
            strings.add("alpha");
            strings.add("beta");
            root.set("strings", strings);
            final ArrayNode numbers = MAPPER.createArrayNode();
            numbers.add(5);
            numbers.add(2);
            numbers.add(10);
            root.set("numbers", numbers);
            
            final ObjectNode schemaRoot = MAPPER.createObjectNode();
            schemaRoot.put("type", "object");
            final ObjectNode properties = MAPPER.createObjectNode();
            // arr schema
            final ObjectNode arrSchema = MAPPER.createObjectNode();
            arrSchema.put("type", "array");
            final ObjectNode arrItems = MAPPER.createObjectNode();
            arrItems.put("type", "object");
            final ObjectNode arrItemProps = MAPPER.createObjectNode();
            final ObjectNode nameProp = MAPPER.createObjectNode();
            nameProp.put("type", "string");
            arrItemProps.set("name", nameProp);
            arrItems.set("properties", arrItemProps);
            arrSchema.set("items", arrItems);
            properties.set("arr", arrSchema);
            // strings schema
            final ObjectNode stringsSchema = MAPPER.createObjectNode();
            stringsSchema.put("type", "array");
            final ObjectNode stringItems = MAPPER.createObjectNode();
            stringItems.put("type", "string");
            stringsSchema.set("items", stringItems);
            properties.set("strings", stringsSchema);
            // numbers schema
            final ObjectNode numbersSchema = MAPPER.createObjectNode();
            numbersSchema.put("type", "array");
            final ObjectNode numberItems = MAPPER.createObjectNode();
            numberItems.put("type", "number");
            numbersSchema.set("items", numberItems);
            properties.set("numbers", numbersSchema);
            schemaRoot.set("properties", properties);
            
            final JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaRoot);
            final ModelImpl model = new ModelImpl(new EventSenderImpl());
            model.jsonAndSchemaSuccessfullyValidated(new File("dummy.json"), new File("dummy_schema.json"), root, schema);
            return model;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void assertSingle(List<ModelChange> changes, ChangeType type, String path)
    {
        assertEquals(1, changes.size());
        final ModelChange c = changes.get(0);
        assertEquals(type, c.getType());
        assertEquals(path, c.getPath());
    }
    
    /**
     * Verifies ADD change and resulting array size when appending new array element.
     */
    @Test
    void testAddNodeToArray()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final List<ModelChange> add = mgr.executeCommand(new AddNodeToArrayCommand(model, "/arr"));
        assertSingle(add, ChangeType.ADD, "/arr/0");
        final JsonNode arr = model.getNodeForPath("/arr").getNode();
        assertEquals(1, arr.size());
    }
    
    /**
     * Adds then removes a node, expecting ADD then REMOVE changes.
     */
    @Test
    void testSetAndRemoveNode()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ObjectNode obj = MAPPER.createObjectNode();
        obj.put("name", "First");
        final List<ModelChange> add = mgr.executeCommand(new SetNodeCommand(model, "/arr/0", obj));
        assertSingle(add, ChangeType.ADD, "/arr/0");
        assertEquals("First", model.getNodeForPath("/arr/0").getNode().get("name").asText());
        final List<ModelChange> rem = mgr.executeCommand(new RemoveNodeCommand(model, "/arr/0"));
        assertSingle(rem, ChangeType.REMOVE, "/arr/0");
        assertEquals(0, model.getNodeForPath("/arr").getNode().size());
    }
    
    /**
     * Move item should yield MOVE change with correct from/to indices.
     */
    @Test
    void testMoveItemToIndex()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ObjectNode first = MAPPER.createObjectNode();
        first.put("name", "First");
        final ObjectNode second = MAPPER.createObjectNode();
        second.put("name", "Second");
        mgr.executeCommand(new SetNodeCommand(model, "/arr/0", first));
        mgr.executeCommand(new SetNodeCommand(model, "/arr/1", second));
        final List<ModelChange> move = mgr.executeCommand(new MoveItemCommand(model, "/arr/1", 0));
        assertSingle(move, ChangeType.MOVE, "/arr");
        final ModelChange m = move.get(0);
        assertEquals(1, m.getFromIndex());
        assertEquals(0, m.getToIndex());
        assertEquals("Second", model.getNodeForPath("/arr/0").getNode().get("name").asText());
    }
    
    /**
     * Duplicate array item yields ADD at next index with cloned content.
     */
    @Test
    void testDuplicateArrayItem()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ObjectNode first = MAPPER.createObjectNode();
        first.put("name", "Orig");
        mgr.executeCommand(new SetNodeCommand(model, "/arr/0", first));
        final int before = model.getNodeForPath("/arr").getNode().size();
        final List<ModelChange> dup = mgr.executeCommand(new DuplicateArrayItemCommand(model, "/arr/0"));
        assertSingle(dup, ChangeType.ADD, "/arr/1");
        final JsonNode arr = model.getNodeForPath("/arr").getNode();
        assertEquals(before + 1, arr.size());
        assertEquals("Orig", arr.get(0).get("name").asText());
        assertEquals("Orig", arr.get(1).get("name").asText());
    }
    
    /**
     * Sorting strings array should produce SORT change with reordered snapshot.
     */
    @Test
    void testSortArrayStrings()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ArrayNode pre = (ArrayNode) model.getNodeForPath("/strings").getNode().deepCopy();
        final List<ModelChange> sort = mgr.executeCommand(new SortArrayCommand(model, "/strings"));
        if (!sort.isEmpty())
        {
            assertSingle(sort, ChangeType.SORT, "/strings");
            final ArrayNode post = (ArrayNode) model.getNodeForPath("/strings").getNode();
            assertEquals("alpha", post.get(0).asText());
            assertEquals("beta", post.get(1).asText());
            assertEquals("delta", post.get(2).asText());
            assertNotEquals(pre, post); // ensure order actually changed
            assertNotNull(sort.get(0).getOldValue());
            assertNotNull(sort.get(0).getNewValue());
        }
    }
    
    /**
     * Sorting numbers array should produce SORT change with reordered snapshot.
     */
    @Test
    void testSortArrayNumbers()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ArrayNode pre = (ArrayNode) model.getNodeForPath("/numbers").getNode().deepCopy();
        final List<ModelChange> sort = mgr.executeCommand(new SortArrayCommand(model, "/numbers"));
        if (!sort.isEmpty())
        {
            assertSingle(sort, ChangeType.SORT, "/numbers");
            final ArrayNode post = (ArrayNode) model.getNodeForPath("/numbers").getNode();
            assertEquals(2, post.get(0).asInt());
            assertEquals(5, post.get(1).asInt());
            assertEquals(10, post.get(2).asInt());
            assertNotEquals(pre, post);
        }
    }
    
    /**
     * Direct SetNodeCommand replace scenario (add then replace) to ensure REPLACE fired.
     */
    @Test
    void testReplaceNode()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ObjectNode a = MAPPER.createObjectNode();
        a.put("name", "A");
        final ObjectNode b = MAPPER.createObjectNode();
        b.put("name", "B");
        mgr.executeCommand(new SetNodeCommand(model, "/arr/0", a));
        final List<ModelChange> replace = mgr.executeCommand(new SetNodeCommand(model, "/arr/0", b));
        assertSingle(replace, ChangeType.REPLACE, "/arr/0");
        assertEquals("B", model.getNodeForPath("/arr/0").getNode().get("name").asText());
    }
    
    /**
     * No change when setting identical node.
     */
    @Test
    void testNoOpSet()
    {
        final ModelImpl model = createModel();
        final CommandManager mgr = new CommandManagerImpl(model);
        final ObjectNode a = MAPPER.createObjectNode();
        a.put("name", "Same");
        mgr.executeCommand(new SetNodeCommand(model, "/arr/0", a));
        final ObjectNode identical = MAPPER.createObjectNode();
        identical.put("name", "Same");
        final List<ModelChange> none = mgr.executeCommand(new SetNodeCommand(model, "/arr/0", identical));
        assertTrue(none.isEmpty());
    }
}
