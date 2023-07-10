package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.control.TableColumn;

public class EditorTableColumn extends TableColumn<JsonNodeWithPath, String>
{
    
    private final String propertyName;
    private final boolean isRequired;
    
    public EditorTableColumn(String propertyName, boolean isRequired)
    {
        super();
        String columnName = propertyName;
        if (isRequired)
        {
            columnName += " *";
        }
        setText(columnName);
        this.propertyName = propertyName;
        this.isRequired = isRequired;
    }
    
    public String getPropertyName()
    {
        return propertyName;
    }
    
    public boolean isRequired()
    {
        return isRequired;
    }
}
