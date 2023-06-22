package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.field;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class BooleanNodeField extends EditorField
{
    public BooleanNodeField(ObjectNode parent, String key)
    {
        super(parent, key);
    }
    
    private void onValueChange(boolean newValue)
    {
        parent.put(key, newValue);
    }
    
    @Override
    protected Node getInputField()
    {
        HBox group = new HBox();
        RadioButton trueButton = new RadioButton("true");
        RadioButton falseButton = new RadioButton("false");
        ToggleGroup toggleGroup = new ToggleGroup();
        trueButton.setToggleGroup(toggleGroup);
        falseButton.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) ->
        {
            if (newValue == trueButton)
            {
                onValueChange(true);
            }
            else if (newValue == falseButton)
            {
                onValueChange(false);
            }
        });
        group.getChildren().addAll(trueButton, falseButton);
        return group;
    }
}
