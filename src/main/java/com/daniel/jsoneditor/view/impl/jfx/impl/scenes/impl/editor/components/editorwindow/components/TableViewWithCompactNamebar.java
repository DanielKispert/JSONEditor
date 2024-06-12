package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CollapseButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TableViewWithCompactNamebar extends VBox
{
    private final EditorTableView tableView;
    
    private final HBox nameBar;
    
    private final CollapseButton collapseButton;
    
    public TableViewWithCompactNamebar(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.tableView = new EditorTableViewImpl(manager, window, model, controller);
        this.nameBar = new HBox();
        this.collapseButton = createCollapseButton(window);
        this.setPrefHeight(5);
        this.getChildren().addAll(nameBar, tableView);
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        tableView.setSelection(selection);
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
}
