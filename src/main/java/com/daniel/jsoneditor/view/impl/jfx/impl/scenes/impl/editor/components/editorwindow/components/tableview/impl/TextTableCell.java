package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.AutofillField;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;

public class TextTableCell extends EditorTableCell
{
    
    private final ReadableModel model;
    
    private final boolean alsoAllowNumbers;
    
    public TextTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean alsoAllowNumbers, boolean holdsObjectKey)
    {
        super(manager, controller, model, holdsObjectKey);
        this.model = model;
        this.alsoAllowNumbers = alsoAllowNumbers;
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        if (alsoAllowNumbers)
        {
            try
            {
                int valueAsInt = Integer.parseInt(newValue);
                item.setProperty(propertyName, valueAsInt);
            }
            catch (NumberFormatException e)
            {
                try
                {
                    double valueAsDouble = Double.parseDouble(newValue);
                    item.setProperty(propertyName, valueAsDouble);
                }
                catch (NumberFormatException f)
                {
                    item.setProperty(propertyName, newValue);
                }
            }
        }
        else
        {
            item.setProperty(propertyName, newValue);
        }
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
            Control fill;
            if (suggestions.getValue().isEmpty())
            {
                fill = new EditorTextField(this, item);
            }
            else
            {
                fill = new AutofillField(this, item, suggestions.getValue(), !suggestions.getKey());
            }
            setGraphic(fill);
            TableColumn<JsonNodeWithPath, String> column = getTableColumn();
            if (column instanceof EditorTableColumn)
            {
                EditorTableColumn editorTableColumn = (EditorTableColumn) column;
                editorTableColumn.setPrefWidthIfHigher(fill.prefWidth(-1));
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
