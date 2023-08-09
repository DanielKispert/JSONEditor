package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.TableCell;

public abstract class EditorTableCell extends TableCell<JsonNodeWithPath, String>
{
    
    private final EditorWindowManager manager;
    
    
    public EditorTableCell(EditorWindowManager manager)
    {
        this.manager = manager;
    }
    
    @Override
    public final void commitEdit(String newValue)
    {
        super.commitEdit(newValue);
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        if (getTableRow() != null && getTableRow().getItem() != null)
        {
            JsonNodeWithPath item = getTableRow().getItem();
            String propertyName = column.getPropertyName();
            JsonNode jsonNode = item.getNode().get(propertyName);
            if (jsonNode != null && jsonNode.isValueNode())
            {
                if (newValue.isEmpty() && !column.isRequired())
                {
                    ((ObjectNode) item.getNode()).remove(propertyName);
                }
                else
                {
                    saveValue(item, propertyName, newValue);
                }
                manager.updateNavbarRepresentation(item.getPath());
            }
        }
    }
    
    protected abstract void saveValue(JsonNodeWithPath item, String propertyName, String newValue);
    
}
