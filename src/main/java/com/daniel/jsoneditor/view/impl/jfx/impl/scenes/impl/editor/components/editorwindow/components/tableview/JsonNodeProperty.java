package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleObjectProperty;

public class JsonNodeProperty extends SimpleObjectProperty<JsonNodeWithPath>
{
    
    public JsonNodeProperty(JsonNodeWithPath node)
    {
        super(node);
    }
    
    @Override
    public String toString()
    {
        JsonNode node = get().getNode();
        if (node != null)
        {
            // Customize the string representation based on the JsonNode type
            if (node.isTextual())
            {
                return node.textValue();
            }
            else if (node.isNumber())
            {
                return String.valueOf(node.numberValue());
            }
            // Add more customization for other JsonNode types if needed
        }
        return super.toString();
    }
}
