package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.AutofillField;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;

public class TextTableCell extends EditorTableCell
{
    
    private final ReadableModel model;
    
    private final boolean alsoAllowNumbers;
    
    public TextTableCell(EditorWindowManager manager, ReadableModel model, boolean alsoAllowNumbers)
    {
        super(manager);
        this.model = model;
        setMaxWidth(Double.MAX_VALUE);
        this.alsoAllowNumbers = alsoAllowNumbers;
    }
    
    public TextTableCell(EditorWindowManager manager, ReadableModel model)
    {
        this(manager, model, false);
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        if (alsoAllowNumbers)
        {
            try
            {
                double valueAsDouble = Double.parseDouble(newValue);
                ((ObjectNode) item.getNode()).put(propertyName, valueAsDouble);
            }
            catch (NumberFormatException e)
            {
                saveAsString(item, propertyName, newValue);
            }
        }
        else
        {
            saveAsString(item, propertyName, newValue);
        }
    }
    
    private void saveAsString(JsonNodeWithPath item, String propertyName, String newValue)
    {
        ((ObjectNode) item.getNode()).put(propertyName, newValue);
    }
    
    @Override
    protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            setGraphic(null);
        }
        else
        {
            Pair<Boolean, List<String>> suggestions = getSuggestions();
            if (suggestions.getValue().isEmpty())
            {
                setGraphic(new EditorTextField(this, item));
            }
            else
            {
                setGraphic(new AutofillField(this, item, suggestions.getValue(), !suggestions.getKey()));
                
            }
        }
    }
    
    private Pair<Boolean, List<String>> getSuggestions()
    {
        if (getTableRow() == null || getTableRow().getItem() == null)
        {
            return new Pair<>(false, Collections.emptyList());
        }
        
        JsonNodeWithPath parentNode = getTableRow().getItem();
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        String propertyName = column.getPropertyName();
        String path = parentNode.getPath() + "/" + propertyName;
        
        final List<String> examples = model.getStringExamplesForPath(path);
        final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
        
        List<String> suggestions;
        if (allowedValues.isEmpty())
        {
            suggestions = examples;
        }
        else
        {
            suggestions = allowedValues;
        }
        
        return new Pair<>(allowedValues.isEmpty(), suggestions);
    }
}
