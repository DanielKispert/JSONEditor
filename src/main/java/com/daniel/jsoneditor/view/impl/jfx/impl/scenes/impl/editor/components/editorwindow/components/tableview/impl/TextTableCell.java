package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextTableCell extends EditorTableCell
{
    
    private final ReadableModel model;
    
    private final TextField textField;
    
    public TextTableCell(EditorWindowManager manager, ReadableModel model)
    {
        super(manager);
        this.model = model;
        textField = new TextField();
    
        {
            textField.setOnAction(event ->
            {
                commitEdit(textField.getText());
            });
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
            {
                if (wasFocused && !isNowFocused)
                {
                    commitEdit(textField.getText());
                }
            });
        }
    }
    
    protected void updateAutofill()
    {
        if (getTableRow() == null || getTableRow().getItem() == null)
        {
            return;
        }
        JsonNodeWithPath parentNode = getTableRow().getItem();
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        String propertyName = column.getPropertyName();
        String path = parentNode.getPath() + "/" + propertyName;
        final List<String> suggestions = model.getStringExamplesForPath(path);
        final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
        
        
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        ((ObjectNode) item.getNode()).put(propertyName, newValue);
    }
    
    @Override
    protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);
        
        if (empty || item == null)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            setText(null);
            textField.setText(item);
            setGraphic(textField);
            updateAutofill();
        }
    }
}
