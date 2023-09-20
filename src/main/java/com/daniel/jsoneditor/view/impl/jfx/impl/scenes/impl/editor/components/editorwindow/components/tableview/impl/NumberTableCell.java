package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import java.math.BigDecimal;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NumberTableCell extends EditorTableCell
{
    public NumberTableCell(EditorWindowManager manager)
    {
        super(manager);
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        try
        {
            int valueAsInt = Integer.parseInt(newValue);
            ((ObjectNode) item.getNode()).put(propertyName, valueAsInt);
        }
        catch (NumberFormatException e)
        {
            try
            {
                double valueAsDouble = Double.parseDouble(newValue);
                ((ObjectNode) item.getNode()).put(propertyName, valueAsDouble);
            }
            catch (NumberFormatException f)
            {
                // value is neither int nor double but in a number field, very problematic
            }
        }
    }
}
