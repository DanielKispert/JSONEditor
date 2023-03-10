package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.field;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * holds the UI to change a field of an object JsonNode
 */
public abstract class EditorField extends VBox
{
    
    protected final ObjectNode parent;
    
    protected final String key;
    
    public EditorField(ObjectNode parent, String key)
    {
        this.key = key;
        this.parent = parent;
        Label fieldTitle = new Label(key);
        getChildren().addAll(fieldTitle, getInputField());
        HBox.setHgrow(this, Priority.ALWAYS);
    }
    
    protected abstract Node getInputField();
}
