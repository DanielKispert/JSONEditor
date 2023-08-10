package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.field.AutofillingComboBox;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.stream.Collectors;


public class TextTableCell extends EditorTableCell
{
    private final ReadableModel model;
    
    public TextTableCell(EditorWindowManager manager, ReadableModel model)
    {
        super(manager);
        this.model = model;
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
            setGraphic(makeComboBox(item));
        }
    }
    
    private AutofillingComboBox makeComboBox(String initialValue)
    {
        JsonNodeWithPath parentNode = getTableRow().getItem();
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        String propertyName = column.getPropertyName();
        String path = parentNode.getPath() + "/" + propertyName;
        final List<String> examples = model.getStringExamplesForPath(path);
        final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
        // if false, the user can enter values that are not in the suggestions. If true, they can't and must pick a value from the suggestions
        boolean userEnteredValuesAllowed;
        List<String> suggestions;
        if (allowedValues.isEmpty())
        {
            suggestions = examples;
            userEnteredValuesAllowed = true;
        }
        else
        {
            suggestions = allowedValues;
            // there are required values, so the user must pick one of them
            userEnteredValuesAllowed = false;
        }
        return new AutofillingComboBox(userEnteredValuesAllowed, initialValue, suggestions);
    }
    
}
