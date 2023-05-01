package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.EditorListView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.impl.EditorListViewImpl;

/*
 * Editor consists of a vbox holding a label and a listview
 *
 */
public class JsonEditorEditorWindow extends VBox
{
    
    private String selectedPath;
    
    private final JsonEditorNamebar nameBar;
    
    private final EditorListView editor;
    
    private final Button addItemButton;
    
    private final ReadableModel model;
    
    public JsonEditorEditorWindow(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.model = model;
        nameBar = new JsonEditorNamebar(manager, this);
        editor = new EditorListViewImpl(manager, model, controller);
        addItemButton = new Button("Add Item");
        addItemButton.setOnAction(event -> controller.addNewNodeToArray(selectedPath));
        HBox.setHgrow(addItemButton, Priority.ALWAYS);
        VBox.setVgrow(addItemButton, Priority.NEVER);
        addItemButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().addAll(nameBar, editor);
    }
    
    public void setSelectedPath(String path)
    {
        this.selectedPath = path;
        JsonNodeWithPath newNode = model.getNodeForPath(path);
        nameBar.setSelection(newNode);
        editor.setSelection(newNode);
        if (model.canAddMoreItems(path))
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
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
}
