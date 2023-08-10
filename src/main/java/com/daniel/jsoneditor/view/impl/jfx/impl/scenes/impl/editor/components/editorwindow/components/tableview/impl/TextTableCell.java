package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TextTableCell extends EditorTableCell
{
    public TextTableCell(EditorWindowManager manager)
    {
        super(manager);
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        ((ObjectNode) item.getNode()).put(propertyName, newValue);
    }
}
