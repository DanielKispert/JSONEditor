package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableViewImpl;

/**
 * Editor consists of a vbox holding:
 * a namebar on top
 * then a table view (the real editor window)
 * and then some buttons.css (if applicable) on the bottom
 *
 */
public class JsonEditorEditorWindow extends VBox
{
    
    private String selectedPath;
    
    private final JsonEditorNamebar nameBar;
    
    private final EditorTableView editor;
    
    private final Button addItemButton;
    
    private final ReadableModel model;
    
    // reference so we don't always have to get it from the path
    private ReferenceableObject displayedObject;
    
    public JsonEditorEditorWindow(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.model = model;
        displayedObject = null;
        nameBar = new JsonEditorNamebar(manager, this, model);
        editor = new EditorTableViewImpl(manager, this, model, controller);
        addItemButton = new Button("Add Item");
        addItemButton.setOnAction(event -> controller.addNewNodeToArray(selectedPath));
        HBox.setHgrow(addItemButton, Priority.ALWAYS);
        VBox.setVgrow(addItemButton, Priority.NEVER);
        addItemButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().addAll(nameBar, editor);
    }
    
    /**
     * selects a json node in this window
     */
    public void setSelectedPath(String path)
    {
        //before we select the object, we divert in case it's an array item and has no object children (we can't drill down more)
        
        this.selectedPath = path;
        JsonNodeWithPath newNode = model.getNodeForPath(path);
        if (newNode.isArray())
        {
            // it should also be fine to get the object of a non-existing node
            displayedObject = model.getReferenceableObject(newNode.getPath() + "/0");
        }
        else if (newNode.isObject())
        {
            displayedObject = model.getReferenceableObject(newNode.getPath());
        }
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
    
    public void focusArrayItem(String itemPath)
    {
        editor.focusItem(itemPath);
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
    
    public ReferenceableObject getDisplayedObject()
    {
        return displayedObject;
    }
}
