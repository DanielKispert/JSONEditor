package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.scene.control.*;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;

import java.util.Iterator;
import java.util.Map;

public class JsonEditorNavbar extends TreeView<JsonNodeWithPath>
{
    private final ReadableModel model;
    
    private final EditorWindowManager editorWindowManager;
    
    private final Controller controller;
    
    private TreeItem<JsonNodeWithPath> selectedItem;
    
    public JsonEditorNavbar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager)
    {
        
        this.model = model;
        this.controller = controller;
        this.editorWindowManager = editorWindowManager;
        this.selectedItem = null;
        SplitPane.setResizableWithParent(this, false);
        setRoot(makeTree());
        setContextMenu(makeContextMenu());
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> JsonEditorNavbar.this.handleNavbarClick(newValue));
        
    }
    
    private ContextMenu makeContextMenu()
    {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem duplicateItem = new MenuItem("Duplicate Item");
        duplicateItem.setOnAction(event ->
        {
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
        
        contextMenu.getItems().add(duplicateItem);
        
        this.setOnContextMenuRequested(event ->
        {
            TreeItem<JsonNodeWithPath> selectedItem = this.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null)
            {
                boolean isArrayItem = selectedItem.getParent().getValue().isArray();
                duplicateItem.setVisible(isArrayItem);
            }
            else
            {
                duplicateItem.setVisible(false);
            }
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
            selectedItem = item;
            // the navbar just tells the editor view to open this node, nothing is sent to the model or controller yet
            editorWindowManager.selectFromNavbar(item.getValue().getPath());
        }
    }
    
    public void updateNavbarItem(String path)
    {
        TreeItem<JsonNodeWithPath> rootItem = getRoot();
        TreeItem<JsonNodeWithPath> itemToUpdate = findNavbarItem(rootItem, path);
        if (itemToUpdate != null)
        {
            itemToUpdate.setValue(model.getNodeForPath(path));
        }
        else
        {
            TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(rootItem, SchemaHelper.getParentPath(path));
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
        TreeItem<JsonNodeWithPath> root = getRoot();
        selectNodeByPath(root, path);
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
            selectedItem = node;
            scrollTo(getRow(selectedItem));
            return;
        }
        for (TreeItem<JsonNodeWithPath> child : node.getChildren())
        {
            selectNodeByPath(child, path);
        }
    }
    
    public void updateTree()
    {
        setRoot(makeTree());
        selectPath(selectedItem.getValue().getPath());
    }
}
