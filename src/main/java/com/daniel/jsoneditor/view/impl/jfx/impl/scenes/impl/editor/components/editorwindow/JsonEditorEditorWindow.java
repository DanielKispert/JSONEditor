package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.AutoAdjustingSplitPane;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.TableViewWithCompactNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Editor consists of a vbox holding:
 * a namebar on top
 * then a table view (the real editor window)
 * and then some buttons.css (if applicable) on the bottom
 */
public class JsonEditorEditorWindow extends VBox
{
    
    private final JsonEditorNamebar nameBar;
    
    private final AutoAdjustingSplitPane editorTables;
    
    private final EditorTableView mainTableView;
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final Button addItemButton;
    
    private final ReadableModel model;
    
    private String selectedPath;
    
    
    private List<TableViewWithCompactNamebar> childTableViews;
    
    // reference so we don't always have to get it from the path
    private ReferenceableObject displayedObject;
    
    public JsonEditorEditorWindow(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.model = model;
        displayedObject = null;
        this.manager = manager;
        this.controller = controller;
        nameBar = new JsonEditorNamebar(manager, this, model);
        childTableViews = new ArrayList<>();
        editorTables = new AutoAdjustingSplitPane();
        editorTables.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainTableView = new EditorTableViewImpl(manager, this, model, controller);
        addItemButton = new Button("Add Item");
        addItemButton.setOnAction(event -> controller.addNewNodeToArray(selectedPath));
        HBox.setHgrow(addItemButton, Priority.ALWAYS);
        VBox.setVgrow(addItemButton, Priority.NEVER);
        addItemButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().addAll(nameBar, editorTables);
    }
    
    /**
     * selects a json node in this window
     */
    public void setSelectedPath(String path)
    {
        this.selectedPath = divertPathToSelect(path);
        JsonNodeWithPath newNode = model.getNodeForPath(selectedPath);
        if (newNode.isArray())
        {
            displayedObject = model.getReferenceableObject(newNode.getPath() + "/0");
        }
        else if (newNode.isObject())
        {
            displayedObject = model.getReferenceableObject(newNode.getPath());
        }
        nameBar.setSelection(newNode);
        mainTableView.setSelection(newNode);
        // for every child node of type array we add a compact child view
        editorTables.getItems().clear();
        editorTables.getItems().add(mainTableView);
        childTableViews = getCompactChildViews(newNode);
        childTableViews.forEach(childTable -> editorTables.getItems().add(childTable)); //intentionally so we send a new event for every add
        if (model.canAddMoreItems(selectedPath))
        {
            if (getChildren().size() == 2)
            {
                getChildren().add(addItemButton);
            }
        }
        else
        {
            getChildren().remove(addItemButton);
        }
        if (!selectedPath.equals(path))
        {
            //if we show a different path than the one that was requested, we show an array. In that case, we focus the item that was requested
            focusArrayItem(path);
        }
    }
    
    private List<TableViewWithCompactNamebar> getCompactChildViews(JsonNodeWithPath node)
    {
        List<TableViewWithCompactNamebar> childViews = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.getNode().fields();
        while (fieldsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            if (entry.getValue().isArray())
            {
                TableViewWithCompactNamebar childView = new TableViewWithCompactNamebar(manager, this, model, controller);
                childView.setSelection(new JsonNodeWithPath(entry.getValue(), node.getPath() + "/" + entry.getKey() ));
                childViews.add(childView);
            }
        }
        return childViews;
    }
    
    /**
     * select the array parent if the node to select is either no object or an object with only non-openable children
     * also, if the parent of the array is an object, then selecting the object would show the array in a "compact child view" anyway.
     * Therefore when we would select an array with an object parent, we select the object parent.
     */
    private String divertPathToSelect(String path)
    {
        String pathToSelect = path;
        JsonNodeWithPath nodeAtPath = model.getNodeForPath(path);
        String parentPath = PathHelper.getParentPath(path);
        if (parentPath != null)
        {
            JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
            if (parentNode.isArray())
            {
                if (!nodeAtPath.isObject())
                {
                    pathToSelect = parentPath;
                }
                else
                {
                    boolean hasObjectOrArrayChildren = false;
                    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = nodeAtPath.getNode().fields();
                    while (fieldsIterator.hasNext())
                    {
                        Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                        if (entry.getValue().isObject() || entry.getValue().isArray())
                        {
                            hasObjectOrArrayChildren = true;
                            break;
                        }
                    }
                    if (!hasObjectOrArrayChildren)
                    {
                        pathToSelect = parentPath;
                    }
                }
            }
            else if (nodeAtPath.isArray() && parentNode.isObject()) //parent object & array child => open the parent too
            {
                pathToSelect = parentPath;
            }
        }
        if (!path.equals(pathToSelect))
        {
            return divertPathToSelect(pathToSelect); //recursion until the path doesn't change
        }
        else
        {
            return pathToSelect;
        }
    }
    
    public void focusArrayItem(String itemPath)
    {
        if (model.getNodeForPath(selectedPath).isObject())
        {
            String parentArrayPath = PathHelper.getParentPath(itemPath);
            for (TableViewWithCompactNamebar childTable : childTableViews)
            {
                if (childTable.getSelectedPath().equals(parentArrayPath))
                {
                    childTable.focusItem(itemPath);
                    return;
                }
            }
        }
        else
        {
            mainTableView.focusItem(itemPath);
        }
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
    
    /**
     * @return the additional tables which are open
     */
    public List<String> getOpenChildPaths()
    {
        List<String> selectedChildPaths = new ArrayList<>();
        for (TableViewWithCompactNamebar childView : childTableViews)
        {
            selectedChildPaths.add(childView.getSelectedPath());
        }
        return selectedChildPaths;
    }
    
    public ReferenceableObject getDisplayedObject()
    {
        return displayedObject;
    }
    
    public AutoAdjustingSplitPane getTablesSplitPane()
    {
        return editorTables;
    }
}
