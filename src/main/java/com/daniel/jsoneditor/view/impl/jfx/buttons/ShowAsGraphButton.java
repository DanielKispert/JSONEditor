package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class ShowAsGraphButton extends Button
{
    private final JsonEditorEditorWindow editorWindow;
    
    private final ReadableModel model;
    
    public ShowAsGraphButton(ReadableModel model, JsonEditorEditorWindow editorWindow)
    {
        super();
        this.editorWindow = editorWindow;
        this.model = model;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_graph_white_24dp.png");
        setOnAction(actionEvent -> handleClick());
        setTooltip(new Tooltip("Show as graph"));
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        String path = selection.getPath();
        boolean visible = model.getReferenceableObject(path) != null && !selection.isArray(); //TODO
        this.setVisible(visible);
        this.setManaged(visible);
    }
    
    private void handleClick()
    {
        editorWindow.showAsGraph();
    }
}
