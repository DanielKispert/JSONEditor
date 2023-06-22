package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EditorTextFieldFactory
{
    public static EditorField makeTextField(ObjectNode parent, String title, JsonNode value)
    {
        if (value != null)
        {
            switch (value.getNodeType())
            {
                case BOOLEAN:
                    return new BooleanNodeField(parent, title);
                case NUMBER:
                    return new NumberNodeField(parent, title);
                case STRING:
                    return new TextNodeField(parent, title);
            }
        }
        return null;
    }
}
