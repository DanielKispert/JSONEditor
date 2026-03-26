package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.commands.CommandManagerImpl;
import com.daniel.jsoneditor.controller.settings.SettingsController;
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
import org.testfx.util.WaitForAsyncUtils;

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
        final SettingsController mockSettings = mock(SettingsController.class);
        when(mockController.getSettingsController()).thenReturn(mockSettings);
        when(mockSettings.hideEmptyColumns()).thenReturn(false);
        final EditorWindowManager mockManager = mock(EditorWindowManager.class);
        final JsonEditorEditorWindow mockWindow = mock(JsonEditorEditorWindow.class);

        tableView = new EditorTableViewImpl(mockManager, mockWindow, model, mockController);

        final Scene scene = new Scene(tableView, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void resetToInitialState()
    {
        // Reset the existing model's data - tableView holds a final reference to the original model instance
        final ModelImpl freshModel = createTestModel();
        model.resetRootNode(freshModel.getRootJson());
        commandManager = new CommandManagerImpl(model);
        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("")));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void shouldUpdateTableWhenItemAdded()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final int initialSize = tableView.getItems().size();

        final ObjectNode newItem = MAPPER.createObjectNode();
        newItem.put("name", "New Item");
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", newItem));

        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemAdded("/items/3"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(initialSize + 1, tableView.getItems().size());
        assertTrue(tableView.getCurrentlyDisplayedPaths().contains("/items/3"));
    }

    @Test
    void shouldUpdateTableWhenItemRemoved()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final int initialSize = tableView.getItems().size();
        assertTrue(initialSize > 0);

        // capture the last item's path before removal to verify total count
        final String lastPath = "/items/" + (initialSize - 1);

        commandManager.executeCommand(new RemoveNodesCommand(model, List.of("/items/0")));

        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemRemoved("/items/0"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(initialSize - 1, tableView.getItems().size());
        // the last path no longer exists after removal (array shrinks by 1)
        assertFalse(tableView.getCurrentlyDisplayedPaths().contains(lastPath));
    }

    @Test
    void shouldUpdateTableWhenItemChanged()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final ObjectNode modifiedItem = MAPPER.createObjectNode();
        modifiedItem.put("name", "Modified Name");
        modifiedItem.put("value", 999);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/0", modifiedItem));

        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemChanged("/items/0/name"));
        WaitForAsyncUtils.waitForFxEvents();

        final JsonNode item = model.getNodeForPath("/items/0").getNode();
        assertEquals("Modified Name", item.get("name").asText());

        final JsonNodeWithPath firstItem = tableView.getItems().stream()
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

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final JsonNode itemAtZero = model.getNodeForPath("/items/0").getNode().deepCopy();

        final List<ModelChange> moveChanges = commandManager.executeCommand(new MoveItemCommand(model, "/items/0", 2));

        if (!moveChanges.isEmpty())
        {
            final ModelChange change = moveChanges.get(0);
            WaitForAsyncUtils.asyncFx(() -> tableView.handleItemMoved(change));
            WaitForAsyncUtils.waitForFxEvents();

            // MoveItemCommand removes item first, then inserts at (targetIndex - 1) when moving forward.
            // Moving index 0 to targetIndex 2 with array size 3: adjustedTarget = 2-1 = 1.
            final int actualLandingIndex = change.getToIndex();
            final JsonNode itemAtLandingIndex = model.getNodeForPath("/items/" + actualLandingIndex).getNode();
            assertEquals(itemAtZero.get("name").asText(), itemAtLandingIndex.get("name").asText());
        }
        else
        {
            fail("MoveItemCommand produced no changes");
        }
    }

    @Test
    void shouldUpdateTableWhenArraySorted()
    {
        resetToInitialState();

        // numbers is an array of primitives - select items (objects) instead for table display
        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, tableView.getItems().size());

        commandManager.executeCommand(new SortArrayCommand(model, "/items"));

        WaitForAsyncUtils.asyncFx(() -> tableView.handleSorted("/items"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, tableView.getItems().size());
    }

    @Test
    void shouldHandleMultipleSequentialChanges()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final int initialSize = tableView.getItems().size();

        final ObjectNode item1 = MAPPER.createObjectNode();
        item1.put("name", "Item A");
        item1.put("value", 100);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", item1));
        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemAdded("/items/3"));
        WaitForAsyncUtils.waitForFxEvents();

        final ObjectNode item2 = MAPPER.createObjectNode();
        item2.put("name", "Item B");
        item2.put("value", 200);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/4", item2));
        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemAdded("/items/4"));
        WaitForAsyncUtils.waitForFxEvents();

        final ObjectNode modifiedItem = MAPPER.createObjectNode();
        modifiedItem.put("name", "Modified Item A");
        modifiedItem.put("value", 150);
        commandManager.executeCommand(new SetNodeCommand(model, "/items/3", modifiedItem));
        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemChanged("/items/3/name"));
        WaitForAsyncUtils.waitForFxEvents();

        commandManager.executeCommand(new RemoveNodesCommand(model, List.of("/items/0")));
        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemRemoved("/items/0"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(initialSize + 1, tableView.getItems().size());
    }

    @Test
    void shouldMaintainTableStateAcrossParentChanges()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, tableView.getItems().size());

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("")));
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(tableView.getItems().isEmpty());

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, tableView.getItems().size());
    }

    @Test
    void shouldHandleNestedObjectPropertyChanges()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("")));
        WaitForAsyncUtils.waitForFxEvents();

        final ObjectNode newConfig = MAPPER.createObjectNode();
        newConfig.put("enabled", false);
        newConfig.put("timeout", 30);
        commandManager.executeCommand(new SetNodeCommand(model, "/config", newConfig));

        // handleItemChanged expects path to the changed property, not the object itself
        WaitForAsyncUtils.asyncFx(() -> tableView.handleItemChanged("/config/enabled"));
        WaitForAsyncUtils.waitForFxEvents();

        final JsonNode config = model.getNodeForPath("/config").getNode();
        assertFalse(config.get("enabled").asBoolean());

        // root object shows itself as the single row, not its child paths
        assertFalse(tableView.getItems().isEmpty());
    }

    @Test
    void shouldFilterAndDisplayCorrectItems()
    {
        resetToInitialState();

        WaitForAsyncUtils.asyncFx(() -> tableView.setSelection(model.getNodeForPath("/items")));
        WaitForAsyncUtils.waitForFxEvents();

        final ObservableList<JsonNodeWithPath> unfilteredItems = tableView.getUnfilteredItems();
        assertEquals(3, unfilteredItems.size());

        WaitForAsyncUtils.asyncFx(() -> tableView.filter());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, tableView.getCurrentlyDisplayedPaths().size());
    }

    private ModelImpl createTestModel()
    {
        final ModelImpl testModel = new ModelImpl(new EventSenderImpl());

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

        // numbers: array of plain numbers - items schema has no properties (primitive)
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
            testModel.jsonAndSchemaSuccessfullyValidated(
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

        return testModel;
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

