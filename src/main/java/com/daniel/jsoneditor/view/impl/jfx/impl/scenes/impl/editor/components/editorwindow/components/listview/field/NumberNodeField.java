package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.field;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.NumberStringConverter;

public class NumberNodeField extends EditorField
{
    private final NumberStringConverter converter;
    public NumberNodeField(ObjectNode parent, String key)
    {
        super(parent, key);
        converter = new NumberStringConverter();
    }
    
    
    protected void onTextChange(Number newValue)
    {
        parent.put(key, newValue.intValue());
    }
    
    @Override
    protected Node getInputField()
    {
        TextField fieldInput = new TextField(parent.get(key).asText());
        fieldInput.textProperty().addListener((observableValue, s, t1) -> onTextChange(converter.fromString(t1)));
        TextFormatter<Number> formatter = new TextFormatter<>(converter);
        fieldInput.setTextFormatter(formatter);
        return fieldInput;
    }
}
