package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ImportDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;


public class EditorNavTree extends TreeView<JsonNodeWithPath> implements NavbarElement
{
    private final ReadableModel model;
    
    private final EditorWindowManager editorWindowManager;
    
    private final Controller controller;
    
    // reference to the stage is required to post a dialog
    private final Stage stage;
    
    private final JsonEditorNavbar navbar;
    
    private String selectedPath;
    
    public EditorNavTree(JsonEditorNavbar navbar, ReadableModel model, Controller controller, EditorWindowManager editorWindowManager,
            Stage stage)
    {
        this.model = model;
        this.controller = controller;
        this.editorWindowManager = editorWindowManager;
        this.stage = stage;
        this.navbar = navbar;
        SplitPane.setResizableWithParent(this, false);
        setRoot(makeTree());
        setContextMenu(makeContextMenu());
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)
            {
                TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem != null)
                {
                    EditorNavTree.this.handleNavbarClick(selectedItem);
                }
            }
        });
        setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
            {
                TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem != null)
                {
                    EditorNavTree.this.handleNavbarClick(selectedItem);
                }
            }
        });
        setCellFactory(tv -> new TreeCell<>()
        {
            private Tooltip tooltip = null;
            
            @Override
            public void updateItem(JsonNodeWithPath item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                {
                    setText(null);
                    setTooltip(null);
                }
                else
                {
                    setText(item.toString());
                    hoverProperty().addListener((observable, wasHovered, isNowHovered) -> {
                        if (isNowHovered && (tooltip == null || !tooltip.getText().equals(item.getDisplayName())))
                        {
                            tooltip = TooltipHelper.makeTooltipFromJsonNode(item.getNode());
                            setTooltip(tooltip);
                        }
                    });
                }
            }
        });
        
        // Add key event handler for copy and paste
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }
    
    private void handleKeyPressed(KeyEvent event)
    {
        if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(event))
        {
            copy();
        }
        else if (new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN).match(event))
        {
            paste();
        }
    }
    
    private void copy()
    {
        TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null)
        {
            controller.copyToClipboard(selectedItem.getValue().getPath());
        }
    }
    
    private void paste()
    {
        TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null)
        {
            controller.pasteFromClipboardReplacingChild(selectedItem.getValue().getPath());
        }
    }
    
    private ContextMenu makeContextMenu()
    {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem addItemItem = new MenuItem("Add Item");
        MenuItem duplicateItem = new MenuItem("Duplicate Item");
        MenuItem newWindowItem = new MenuItem("Open in new Window");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem sortArray = new MenuItem("Sort");
        MenuItem importItem = new MenuItem("Import");
        MenuItem exportItem = new MenuItem("Export");
        MenuItem exportWithDependenciesItem = new MenuItem("Export with Dependencies");
        
        copyItem.setOnAction(event -> copy());
        pasteItem.setOnAction(event -> paste());
        addItemItem.setOnAction(event -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                controller.addNewNodeToArray(selectedNode.getPath());
                
            }
        });
        duplicateItem.setOnAction(event -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null)
            {
                TreeItem<JsonNodeWithPath> parentItem = selectedItem.getParent();
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                
                if (parentItem.getValue().isArray())
                {
                    controller.duplicateArrayNode(selectedNode.getPath());
                }
            }
        });
        newWindowItem.setOnAction(actionEvent -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                //directly opening from the navbar should not open the parent
                editorWindowManager.openInNewWindowIfPossible(selectedNode.getPath(), false);
            }
        });
        sortArray.setOnAction(actionEvent -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                controller.sortArray(selectedNode.getPath());
            }
        });
        deleteItem.setOnAction(actionEvent -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                controller.removeNode(selectedNode.getPath());
            }
        });
        importItem.setOnAction(event -> {
            ImportDialog importDialog = new ImportDialog(stage);
            Optional<String> importResult = importDialog.showAndWait();
            if (importResult.isPresent())
            {
                String jsonToImport = importResult.get();
                jsonToImport = controller.resolveVariablesInJson(jsonToImport);
                TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
                JsonNodeWithPath selectedNode = selectedItem.getValue();
                controller.importAtNode(selectedNode.getPath(), jsonToImport);
            }
        });
        exportItem.setOnAction(event -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            JsonNodeWithPath selectedNode = selectedItem.getValue();
            controller.exportNode(selectedNode.getPath());
        });
        exportWithDependenciesItem.setOnAction(event -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            JsonNodeWithPath selectedNode = selectedItem.getValue();
            controller.exportNodeWithDependencies(selectedNode.getPath());
        });
        
        contextMenu.getItems().addAll(newWindowItem, addItemItem, copyItem, pasteItem, duplicateItem, sortArray, importItem, exportItem,
                exportWithDependenciesItem, deleteItem);
        
        this.setOnContextMenuRequested(event -> {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                if (selectedItem.getParent() != null)
                {
                    boolean isArrayItem = selectedItem.getParent().getValue().isArray();
                    duplicateItem.setVisible(isArrayItem);
                }
                if (selectedItem.getValue().isArray())
                {
                    addItemItem.setVisible(true);
                    sortArray.setVisible(true);
                }
                else
                {
                    addItemItem.setVisible(false);
                    sortArray.setVisible(false);
                }
            }
            else
            {
                duplicateItem.setVisible(false);
            }
            // only show the prompt to display in a new window if the maximum window amount is not reached
            newWindowItem.setVisible(editorWindowManager.canAnotherWindowBeAdded());
            
        });
        
        return contextMenu;
    }
    
    private NavbarItem makeTree()
    {
        model.getRootJson();
        NavbarItem root = new NavbarItem(model, "");
        root.setExpanded(true);
        populateItem(root);
        return root;
    }
    
    private void populateItem(NavbarItem item)
    {
        JsonNode node = item.getValue().getNode();
        if (node.getNodeType().equals(JsonNodeType.OBJECT))
        {
            populateForObject(item);
        }
        else if (JsonNodeType.ARRAY.equals(node.getNodeType()))
        {
            populateForArray(item);
        }
    }
    
    private void populateForObject(NavbarItem parent)
    {
        Iterator<Map.Entry<String, JsonNode>> fields = parent.getValue().getNode().fields();
        String pathForFields = parent.getValue().getPath() + "/";
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (value.getNodeType() == JsonNodeType.OBJECT || value.getNodeType() == JsonNodeType.ARRAY)
            {
                String pathForNode = pathForFields + field.getKey();
                NavbarItem child = new NavbarItem(model, pathForNode);
                populateItem(child);
                parent.getChildren().add(child);
            }
        }
    }
    
    private void populateForArray(NavbarItem parent)
    {
        String pathForItems = parent.getValue().getPath() + "/";
        int index = 0;
        for (JsonNode item : parent.getValue().getNode())
        {
            if (item.getNodeType() == JsonNodeType.OBJECT || item.getNodeType() == JsonNodeType.ARRAY)
            {
                String pathForNode = pathForItems + index++;
                NavbarItem child = new NavbarItem(model, pathForNode);
                populateItem(child);
                parent.getChildren().add(child);
            }
        }
    }
    
    private void handleNavbarClick(TreeItem<JsonNodeWithPath> item)
    {
        if (item != null)
        {
            String path = item.getValue().getPath();
            this.selectedPath = path;
            // the navbar just tells the editor view to open this node, nothing is sent to the model or controller yet
            editorWindowManager.openPath(path, false); //directly opening an item should not open its parent
            navbar.selectPath(path);
        }
    }
    
    @Override
    public void updateSingleElement(String path)
    {
        TreeItem<JsonNodeWithPath> rootItem = getRoot();
        TreeItem<JsonNodeWithPath> itemToUpdate = findNavbarItem(rootItem, path);
        if (itemToUpdate != null)
        {
            itemToUpdate.setValue(model.getNodeForPath(path));
        }
        else
        {
            TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(rootItem, PathHelper.getParentPath(path));
            if (parentItem != null)
            {
                JsonNodeWithPath newNode = model.getNodeForPath(path);
                NavbarItem newItem = new NavbarItem(model, newNode.getPath());
                populateItem(newItem);
                parentItem.getChildren().add(newItem);
                
            }
        }
    }
    
    private TreeItem<JsonNodeWithPath> findNavbarItem(TreeItem<JsonNodeWithPath> currentItem, String path)
    {
        if (currentItem.getValue().getPath().equals(path))
        {
            return currentItem;
        }
        
        for (TreeItem<JsonNodeWithPath> childItem : currentItem.getChildren())
        {
            TreeItem<JsonNodeWithPath> foundItem = findNavbarItem(childItem, path);
            if (foundItem != null)
            {
                return foundItem;
            }
        }
        
        return null;
    }
    
    public void selectPath(String path)
    {
        if (!Objects.equals(selectedPath, path))
        {
            selectedPath = path;
            TreeItem<JsonNodeWithPath> root = getRoot();
            selectNodeByPath(root, path);
        }
    }
    
    private void selectNodeByPath(TreeItem<JsonNodeWithPath> node, String path)
    {
        if (node == null || node.getValue() == null)
        {
            return;
        }
        if (node.getValue().getPath().equals(path))
        {
            getSelectionModel().select(node);
            scrollTo(getRow(node));
            return;
        }
        for (TreeItem<JsonNodeWithPath> child : node.getChildren())
        {
            selectNodeByPath(child, path);
        }
    }
    
    @Override
    public void updateView()
    {
        setRoot(makeTree());
    }
    
    // Granular update methods for specific model changes
    public void handlePathAdded(String path)
    {
        final TreeItem<JsonNodeWithPath> rootItem = getRoot();
        final String parentPath = PathHelper.getParentPath(path);
        final TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(rootItem, parentPath);
        
        if (parentItem != null)
        {
            final JsonNodeWithPath newNode = model.getNodeForPath(path);
            if (newNode != null && (newNode.isObject() || newNode.isArray()))
            {
                final NavbarItem newItem = new NavbarItem(model, path);
                populateItem(newItem);
                parentItem.getChildren().add(newItem);
                parentItem.setExpanded(true); // expand parent to show new item
            }
        }
    }
    
    public void handlePathRemoved(String path)
    {
        final TreeItem<JsonNodeWithPath> rootItem = getRoot();
        final TreeItem<JsonNodeWithPath> itemToRemove = findNavbarItem(rootItem, path);
        
        if (itemToRemove != null && itemToRemove.getParent() != null)
        {
            itemToRemove.getParent().getChildren().remove(itemToRemove);
            
            // Clear selection if removed item was selected
            if (path.equals(selectedPath))
            {
                getSelectionModel().clearSelection();
                selectedPath = null;
            }
        }
    }
    
    public void handlePathChanged(String path)
    {
        updateSingleElement(path);
    }
    
    public void handlePathMoved(String path)
    {
        // For moves within arrays, refresh the parent array structure
        final String parentPath = PathHelper.getParentPath(path);
        final TreeItem<JsonNodeWithPath> rootItem = getRoot();
        final TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(rootItem, parentPath);
        
        if (parentItem != null)
        {
            // Clear and rebuild children to reflect new order
            parentItem.getChildren().clear();
            populateItem((NavbarItem) parentItem);
        }
    }
    
    public void handlePathSorted(String path)
    {
        // Refresh the sorted array to show new order
        final TreeItem<JsonNodeWithPath> rootItem = getRoot();
        final TreeItem<JsonNodeWithPath> sortedItem = findNavbarItem(rootItem, path);
        
        if (sortedItem != null)
        {
            sortedItem.getChildren().clear();
            populateItem((NavbarItem) sortedItem);
        }
    }
    
    public void handleRemovedSelection(String path)
    {
        if (path.equals(selectedPath))
        {
            getSelectionModel().clearSelection();
            selectedPath = null;
        }
    }
    
    public void handleSettingsChanged()
    {
        // Refresh tree view to apply new settings (e.g., display preferences)
        updateView();
    }
}
