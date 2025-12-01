package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.commands.CommandManagerImpl;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.impl.*;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ModelChangeUIIntegrationTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private ModelImpl model;
    private CommandManager commandManager;
    private EditorTableViewImpl tableView;
    
    @Start
    void start(Stage stage)
    {
        model = createTestModel();
        commandManager = new CommandManagerImpl(model);
        final Controller mockController = mock(Controller.class);
        final EditorWindowManager mockManager = mock(EditorWindowManager.class);
        final JsonEditorEditorWindow mockWindow = mock(JsonEditorEditorWindow.class);
        
        tableView = new EditorTableViewImpl(mockManager, mockWindow, model, mockController);
        
        final Scene scene = new Scene(tableView, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private void resetToInitialState()
    {
        model = createTestModel();
        commandManager = new CommandManagerImpl(model);
        final JsonNodeWithPath root = model.getNodeForPath("");
        tableView.setSelection(root);
    }
    
    @Test
    void shouldUpdateTableWhenItemAdded()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/items");
        tableView.setSelection(arrayNode);
        
        final int initialSize = tableView.getItems().size();
        
        final ObjectNode newItem = MAPPER.createObjectNode();
        newItem.put("name", "New Item");
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", newItem));
        
        tableView.handleItemAdded("/items/3");
        
        assertEquals(initialSize + 1, tableView.getItems().size());
        
        final List<String> displayedPaths = tableView.getCurrentlyDisplayedPaths();
        assertTrue(displayedPaths.contains("/items/3"));
    }
    
    @Test
    void shouldUpdateTableWhenItemRemoved()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/items");
        tableView.setSelection(arrayNode);
        
        final int initialSize = tableView.getItems().size();
        assertTrue(initialSize > 0);
        
        commandManager.executeCommand(new RemoveNodesCommand(model, List.of("/items/0")));
        
        tableView.handleItemRemoved("/items/0");
        
        assertEquals(initialSize - 1, tableView.getItems().size());
        
        final List<String> displayedPaths = tableView.getCurrentlyDisplayedPaths();
        assertFalse(displayedPaths.contains("/items/0"));
    }
    
    @Test
    void shouldUpdateTableWhenItemChanged()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/items");
        tableView.setSelection(arrayNode);
        
        final ObjectNode modifiedItem = MAPPER.createObjectNode();
        modifiedItem.put("name", "Modified Name");
        modifiedItem.put("value", 999);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/0", modifiedItem));
        
        tableView.handleItemChanged("/items/0");
        
        final JsonNode item = model.getNodeForPath("/items/0").getNode();
        assertEquals("Modified Name", item.get("name").asText());
        
        final ObservableList<JsonNodeWithPath> items = tableView.getItems();
        final JsonNodeWithPath firstItem = items.stream()
                .filter(i -> i.getPath().equals("/items/0"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(firstItem);
        assertEquals("Modified Name", firstItem.getNode().get("name").asText());
    }
    
    @Test
    void shouldUpdateTableWhenItemMoved()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/items");
        tableView.setSelection(arrayNode);
        
        final JsonNode itemAtZero = model.getNodeForPath("/items/0").getNode().deepCopy();
        
        final List<ModelChange> moveChanges = commandManager.executeCommand(new MoveItemCommand(model, "/items/0", 2));
        
        if (!moveChanges.isEmpty())
        {
            tableView.handleItemMoved(moveChanges.get(0));
        }
        
        final JsonNode newItemAtTwo = model.getNodeForPath("/items/2").getNode();
        assertEquals(itemAtZero.get("name").asText(), newItemAtTwo.get("name").asText());
    }
    
    @Test
    void shouldUpdateTableWhenArraySorted()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/numbers");
        tableView.setSelection(arrayNode);
        
        commandManager.executeCommand(new SortArrayCommand(model, "/numbers"));
        
        tableView.handleSorted("/numbers");
        
        assertEquals(3, tableView.getItems().size());
        
        final ArrayNode sorted = (ArrayNode) model.getNodeForPath("/numbers").getNode();
        assertEquals(1, sorted.get(0).asInt());
        assertEquals(3, sorted.get(1).asInt());
        assertEquals(5, sorted.get(2).asInt());
    }
    
    @Test
    void shouldHandleMultipleSequentialChanges()
    {
        resetToInitialState();
        
        final JsonNodeWithPath arrayNode = model.getNodeForPath("/items");
        tableView.setSelection(arrayNode);
        
        final int initialSize = tableView.getItems().size();
        
        final ObjectNode item1 = MAPPER.createObjectNode();
        item1.put("name", "Item A");
        item1.put("value", 100);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", item1));
        tableView.handleItemAdded("/items/3");
        
        final ObjectNode item2 = MAPPER.createObjectNode();
        item2.put("name", "Item B");
        item2.put("value", 200);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/4", item2));
        tableView.handleItemAdded("/items/4");
        
        final ObjectNode modifiedItem = MAPPER.createObjectNode();
        modifiedItem.put("name", "Modified Item A");
        modifiedItem.put("value", 150);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", modifiedItem));
        tableView.handleItemChanged("/items/3");
        
        commandManager.executeCommand(new RemoveNodesCommand(model, List.of("/items/0")));
        tableView.handleItemRemoved("/items/0");
        
        final int finalSize = tableView.getItems().size();
        assertEquals(initialSize + 1, finalSize);
    }
    
    @Test
    void shouldMaintainTableStateAcrossParentChanges()
    {
        resetToInitialState();
        
        final JsonNodeWithPath itemsNode = model.getNodeForPath("/items");
        tableView.setSelection(itemsNode);
        
        assertEquals(3, tableView.getItems().size());
        
        final JsonNodeWithPath rootNode = model.getNodeForPath("");
        tableView.setSelection(rootNode);
        
        assertFalse(tableView.getItems().isEmpty());
        
        tableView.setSelection(itemsNode);
        assertEquals(3, tableView.getItems().size());
    }
    
    @Test
    void shouldHandleNestedObjectPropertyChanges()
    {
        resetToInitialState();
        
        final JsonNodeWithPath rootNode = model.getNodeForPath("");
        tableView.setSelection(rootNode);
        
        final ObjectNode newConfig = MAPPER.createObjectNode();
        newConfig.put("enabled", false);
        newConfig.put("timeout", 30);
        commandManager.executeCommand(new SetNodeCommand(model, "/config", newConfig));
        
        tableView.handleItemChanged("/config");
        
        final JsonNode config = model.getNodeForPath("/config").getNode();
        assertFalse(config.get("enabled").asBoolean());
        
        final List<String> displayedPaths = tableView.getCurrentlyDisplayedPaths();
        assertTrue(displayedPaths.contains("/config"));
    }
    
    @Test
    void shouldFilterAndDisplayCorrectItems()
    {
        resetToInitialState();
        
        final JsonNodeWithPath itemsNode = model.getNodeForPath("/items");
        tableView.setSelection(itemsNode);
        
        final ObservableList<JsonNodeWithPath> unfilteredItems = tableView.getUnfilteredItems();
        assertEquals(3, unfilteredItems.size());
        
        tableView.filter();
        
        final List<String> displayedPaths = tableView.getCurrentlyDisplayedPaths();
        assertEquals(3, displayedPaths.size());
    }
    
    private ModelImpl createTestModel()
    {
        final ModelImpl model = new ModelImpl(new EventSenderImpl());
        
        final ObjectNode root = MAPPER.createObjectNode();
        
        final ArrayNode items = MAPPER.createArrayNode();
        for (int i = 0; i < 3; i++)
        {
            final ObjectNode item = MAPPER.createObjectNode();
            item.put("name", "Item " + i);
            item.put("value", i * 10);
            items.add(item);
        }
        root.set("items", items);
        
        final ArrayNode numbers = MAPPER.createArrayNode();
        numbers.add(5);
        numbers.add(3);
        numbers.add(1);
        root.set("numbers", numbers);
        
        final ObjectNode config = MAPPER.createObjectNode();
        config.put("enabled", true);
        config.put("timeout", 30);
        root.set("config", config);
        
        final ObjectNode schemaRoot = MAPPER.createObjectNode();
        schemaRoot.put("type", "object");
        final ObjectNode properties = MAPPER.createObjectNode();
        
        final ObjectNode itemsSchema = MAPPER.createObjectNode();
        itemsSchema.put("type", "array");
        final ObjectNode itemSchema = MAPPER.createObjectNode();
        itemSchema.put("type", "object");
        final ObjectNode itemProperties = MAPPER.createObjectNode();
        itemProperties.set("name", createStringSchema());
        itemProperties.set("value", createNumberSchema());
        itemSchema.set("properties", itemProperties);
        itemsSchema.set("items", itemSchema);
        properties.set("items", itemsSchema);
        
        final ObjectNode numbersSchema = MAPPER.createObjectNode();
        numbersSchema.put("type", "array");
        numbersSchema.set("items", createNumberSchema());
        properties.set("numbers", numbersSchema);
        
        final ObjectNode configSchema = MAPPER.createObjectNode();
        configSchema.put("type", "object");
        final ObjectNode configProperties = MAPPER.createObjectNode();
        configProperties.set("enabled", createBooleanSchema());
        configProperties.set("timeout", createNumberSchema());
        configSchema.set("properties", configProperties);
        properties.set("config", configSchema);
        
        schemaRoot.set("properties", properties);
        
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(schemaRoot);
        
        try
        {
            model.jsonAndSchemaSuccessfullyValidated(
                new java.io.File("test.json"),
                new java.io.File("test_schema.json"),
                root,
                schema
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize test model", e);
        }
        
        return model;
    }
    
    private ObjectNode createStringSchema()
    {
        final ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "string");
        return schema;
    }
    
    private ObjectNode createNumberSchema()
    {
        final ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "number");
        return schema;
    }
    
    private ObjectNode createBooleanSchema()
    {
        final ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "boolean");
        return schema;
    }
}

