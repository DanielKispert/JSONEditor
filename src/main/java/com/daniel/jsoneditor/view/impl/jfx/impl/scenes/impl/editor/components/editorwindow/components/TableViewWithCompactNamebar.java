package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TableViewWithCompactNamebar extends VBox
{
    private final EditorTableView tableView;
    
    private final HBox nameBar;
    
    public TableViewWithCompactNamebar(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.tableView = new EditorTableViewImpl(manager, window, model, controller);
        this.nameBar = new HBox();
        this.getChildren().addAll(nameBar, tableView);
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        tableView.setSelection(selection);
        // the namebar gets the fancy name of the selected node
        nameBar.getChildren().clear();
        // make a label for it
        Label label = new Label(selection.getDisplayName());
        nameBar.getChildren().add(label);
    }
    
    public void focusItem(String itemPath)
    {
        tableView.focusItem(itemPath);
    }
}
