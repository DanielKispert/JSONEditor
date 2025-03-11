package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;


public class NumberTableCell extends EditorTableCell
{
    public NumberTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean holdsObjectKey)
    {
        super(manager, controller, model, holdsObjectKey);
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
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
                // value is neither int nor double but in a number field, very problematic
            }
        }
    }
}
