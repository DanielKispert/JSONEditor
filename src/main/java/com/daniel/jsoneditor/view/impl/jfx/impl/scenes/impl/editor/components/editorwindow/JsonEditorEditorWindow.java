package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.AutoAdjustingSplitPane;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.TableViewWithCompactNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TableViewButtonBar;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
    
    private final ReadableModel model;
    
    private String selectedPath;
    
    private final TableViewButtonBar buttonBar;
    
    private List<TableViewWithCompactNamebar> childTableViews;
    
    public JsonEditorEditorWindow(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.model = model;
        this.manager = manager;
        this.controller = controller;
        nameBar = new JsonEditorNamebar(manager, this, model, controller);
        childTableViews = new ArrayList<>();
        editorTables = new AutoAdjustingSplitPane();
        editorTables.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainTableView = new EditorTableViewImpl(manager, this, model, controller);
        buttonBar = new TableViewButtonBar(model, controller, mainTableView::getCurrentlyDisplayedPaths, () -> selectedPath);
        
        VBox.setVgrow(buttonBar, Priority.NEVER);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().addAll(nameBar, editorTables, buttonBar);
    }
    
    /**
     * helper method that just always allows diverting to child views
     */
    public void setSelectedPath(String path)
    {
        setSelectedPath(path, true);
    }
    
    /**
     * selects a json node in this window
     *
     * @param allowDivertingToChildViews if true, then opening an array will lead to opening its object parent so the array is shown in a
     *                                   child view
     */
    public void setSelectedPath(String path, boolean allowDivertingToChildViews)
    {
        this.selectedPath = divertPathToSelect(path, allowDivertingToChildViews);
        JsonNodeWithPath newNode = model.getNodeForPath(selectedPath);
        nameBar.setSelection(newNode);
        mainTableView.setSelection(newNode);
        // for every child node of type array we add a compact child view
        editorTables.getItems().clear();
        editorTables.getItems().add(mainTableView);
        childTableViews = getCompactChildViews(newNode);
        childTableViews.forEach(childTable -> editorTables.getItems().add(childTable)); //intentionally so we send a new event for every add
        buttonBar.updateBottomBar(model.canAddMoreItems(path), false);
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
                childView.setSelection(new JsonNodeWithPath(entry.getValue(), node.getPath() + "/" + entry.getKey()));
                childViews.add(childView);
            }
        }
        return childViews;
    }
    
    /**
     * select the array parent if the node to select is either no object or an object with only non-openable children
     * also, if the parent of the array is an object, then selecting the object would show the array in a "compact child view" anyway.
     * Therefore when we would select an array with an object parent, we select the object parent if the parameter allows it.
     */
    private String divertPathToSelect(String path, boolean allowDivertingToChildViews)
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
            else if (allowDivertingToChildViews && nodeAtPath.isArray() && parentNode.isObject()) //parent object & array child => open the
            // parent too
            {
                pathToSelect = parentPath;
            }
        }
        if (!path.equals(pathToSelect))
        {
            return divertPathToSelect(pathToSelect, allowDivertingToChildViews); //recursion until the path doesn't change
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
        return childTableViews.stream().map(TableViewWithCompactNamebar::getSelectedPath).collect(Collectors.toList());
    }
    
    public AutoAdjustingSplitPane getTablesSplitPane()
    {
        return editorTables;
    }
    
    public void handleChildAdded(String path)
    {
        final String parentPath = PathHelper.getParentPath(path);
        
        if (selectedPath.equals(parentPath))
        {
            mainTableView.handleItemAdded(path);
        }
        
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            if (childTable.getSelectedPath().equals(parentPath))
            {
                childTable.handleItemAdded(path);
            }
        }
    }
    
    public void handleChildRemoved(String path)
    {
        final String parentPath = PathHelper.getParentPath(path);
        
        if (selectedPath.equals(parentPath))
        {
            mainTableView.handleItemRemoved(path);
        }
        
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            if (childTable.getSelectedPath().equals(parentPath))
            {
                childTable.handleItemRemoved(path);
            }
        }
    }
    
    public void handlePathChanged(String path)
    {
        if (selectedPath.equals(path))
        {
            mainTableView.handleItemChanged(path);
        }
        
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            if (childTable.getSelectedPath().equals(path) || path.startsWith(childTable.getSelectedPath() + "/"))
            {
                childTable.handleItemChanged(path);
            }
        }
    }
    
    public void handleChildMoved(ModelChange change)
    {
        final String path = change.getPath();
        
        if (selectedPath.equals(path))
        {
            mainTableView.handleItemMoved(change);
        }
        
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            if (childTable.getSelectedPath().equals(path))
            {
                childTable.handleItemMoved(change);
            }
        }
    }
    
    public void handleSorted(String path)
    {
        if (selectedPath.equals(path))
        {
            mainTableView.handleSorted(path);
        }
        
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            if (childTable.getSelectedPath().equals(path))
            {
                childTable.handleSorted(path);
            }
        }
    }
    
    /**
     * Refreshes visibility toggle buttons when hideEmptyColumns setting changes
     */
    public void refreshVisibilityToggleButtons()
    {
        for (TableViewWithCompactNamebar childTable : childTableViews)
        {
            childTable.refreshButtonVisibility();
        }
    }
    
    /**
     * Called when settings are changed to update UI accordingly
     */
    public void handleSettingsChanged()
    {
        refreshVisibilityToggleButtons();
        // Refresh the entire window to apply new settings
        setSelectedPath(selectedPath);
    }
    
    @Override
    protected double computePrefWidth(double v)
    {
        // the preferred width of the editor window is the highest width of all its children, so nameBar, editorTables and buttonBar
        double prefWidth = Math.max(nameBar.prefWidth(v), editorTables.prefWidth(v));
        return Math.max(prefWidth, buttonBar.prefWidth(v));
    }
}
