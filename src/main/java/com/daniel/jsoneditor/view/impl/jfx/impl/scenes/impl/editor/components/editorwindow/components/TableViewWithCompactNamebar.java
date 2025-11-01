package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CollapseButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.VisibilityToggleButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.elements.Collapsible;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TableViewButtonBar;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class TableViewWithCompactNamebar extends VBox implements Collapsible
{
    private final EditorTableView tableView;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    private final HBox nameBar;
    
    private final CollapseButton collapseButton;
    
    private final TableViewButtonBar buttonBar;
    
    private final VisibilityToggleButton visibilityToggleButton;
    
    private boolean collapsed;
    
    private String selectedPath;
    
    public TableViewWithCompactNamebar(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model,
            Controller controller)
    {
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.tableView = new EditorTableViewImpl(manager, window, model, controller);
        this.nameBar = new HBox();
        this.controller = controller;
        this.model = model;
        this.buttonBar = new TableViewButtonBar(model, controller, tableView::getCurrentlyDisplayedPaths, () -> selectedPath);
        this.collapseButton = createCollapseButton(window);
        this.visibilityToggleButton = new VisibilityToggleButton(tableView);
        this.getChildren().addAll(nameBar, tableView, buttonBar);
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        this.selectedPath = selection.getPath();
        tableView.setSelection(selection);
        
        // the namebar gets the fancy name of the selected node
        nameBar.getChildren().clear();
        nameBar.getChildren().add(createNameLabel(selection.getDisplayName()));
        
        // Add visibility toggle button if hideEmptyColumns setting is enabled and this is an array
        if (controller.getSettingsController().hideEmptyColumns() && model.getNodeForPath(selectedPath).isArray())
        {
            nameBar.getChildren().add(visibilityToggleButton);
        }
        
        buttonBar.updateBottomBar(model.canAddMoreItems(selectedPath), !tableView.getCurrentlyDisplayedPaths().isEmpty());
        //nameBar.getChildren().addAll(collapseButton, createNameLabel(selection.getDisplayName()));
    }
    
    private Label createNameLabel(String displayName)
    {
        // the label should align with the center vertically and get some padding, too
        Label label = new Label(displayName);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPadding(new Insets(5, 5, 5, 5));
        
        return label;
    }
    
    @Override
    protected double computeMinHeight(double v)
    {
        return nameBar.getHeight();
    }
    
    @Override
    protected double computePrefHeight(double v)
    {
        if (collapsed)
        {
            return nameBar.getHeight();
        }
        else
        {
            return nameBar.getHeight() + tableView.prefHeight(v) + buttonBar.prefHeight(v);
        }
    }
    
    private CollapseButton createCollapseButton(JsonEditorEditorWindow window)
    {
        return new CollapseButton(tableView, window);
    }
    
    @Override
    public void collapse()
    {
        tableView.setVisible(false);
        buttonBar.setVisible(false);
        collapsed = true;
    }
    
    @Override
    public void expand()
    {
        tableView.setVisible(true);
        buttonBar.setVisible(true);
        collapsed = false;
    }
    
    @Override
    public boolean isCollapsed()
    {
        return collapsed;
    }
    
    public void focusItem(String itemPath)
    {
        tableView.focusItem(itemPath);
    }
    
    public void refreshButtonVisibility()
    {
        if (selectedPath != null)
        {
            // Re-evaluate if button should be visible
            nameBar.getChildren().clear();
            nameBar.getChildren().add(createNameLabel(model.getNodeForPath(selectedPath).getDisplayName()));
            
            if (controller.getSettingsController().hideEmptyColumns() && model.getNodeForPath(selectedPath).isArray())
            {
                nameBar.getChildren().add(visibilityToggleButton);
            }
            
            // Refresh the table to apply new settings
            tableView.setSelection(model.getNodeForPath(selectedPath));
        }
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
    
    // Granular update methods for specific model changes
    public void handleItemAdded(String path)
    {
        tableView.handleItemAdded(path);
        buttonBar.updateBottomBar(model.canAddMoreItems(selectedPath), !tableView.getCurrentlyDisplayedPaths().isEmpty());
    }
    
    public void handleItemRemoved(String path)
    {
        tableView.handleItemRemoved(path);
        buttonBar.updateBottomBar(model.canAddMoreItems(selectedPath), !tableView.getCurrentlyDisplayedPaths().isEmpty());
    }
    
    public void handleItemChanged(String path)
    {
        tableView.handleItemChanged(path);
    }
    
    public void handleItemMoved(String path)
    {
        tableView.handleItemMoved(path);
    }
    
    public void handleSorted(String path)
    {
        tableView.handleSorted(path);
    }
}
