package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CollapseButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.elements.Collapsible;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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
    
    private final Button addItemButton;
    
    private boolean collapsed;
    
    private String selectedPath;
    
    public TableViewWithCompactNamebar(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.tableView = new EditorTableViewImpl(manager, window, model, controller);
        this.nameBar = new HBox();
        this.controller = controller;
        this.model = model;
        this.collapseButton = createCollapseButton(window);
        addItemButton = new Button("Add Item");
        this.setPrefHeight(5);
        this.getChildren().addAll(nameBar, tableView);
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        this.selectedPath = selection.getPath();
        tableView.setSelection(selection);
        if (model.canAddMoreItems(selection.getPath()))
        {
            this.getChildren().add(addItemButton);
            addItemButton.setOnAction(event -> controller.addNewNodeToArray(selection.getPath()));
        }
        else
        {
            this.getChildren().remove(addItemButton);
        }
        // the namebar gets the fancy name of the selected node
        nameBar.getChildren().clear();
        nameBar.getChildren().addAll(collapseButton, createNameLabel(selection.getDisplayName()));
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
        return super.computePrefHeight(v);
    }
    
    @Override
    protected double computePrefWidth(double v)
    {
        return super.computePrefWidth(v);
    }
    
    private CollapseButton createCollapseButton(JsonEditorEditorWindow window)
    {
        CollapseButton collapseButton = new CollapseButton(tableView, window);
        //the button should only be as large as the label
        return collapseButton;
    }
    
    public void focusItem(String itemPath)
    {
        tableView.focusItem(itemPath);
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
    
    @Override
    public void collapse()
    {
        collapsed = true;
        tableView.setVisible(false);
    }
    
    @Override
    public void expand()
    {
        collapsed = false;
        tableView.setVisible(true);
    }
    
    @Override
    public boolean isCollapsed()
    {
        return collapsed;
    }
}
