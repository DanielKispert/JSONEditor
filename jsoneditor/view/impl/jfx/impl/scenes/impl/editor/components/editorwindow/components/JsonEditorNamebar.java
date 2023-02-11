package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import javafx.scene.control.Label;
import jsoneditor.model.json.JsonNodeWithPath;

public class JsonEditorNamebar extends Label
{
    public JsonEditorNamebar()
    {
        super();
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        setText(makeFancyName(selection));
    }
    
    private String makeFancyName(JsonNodeWithPath node)
    {
        String path = node.getPath();
        String displayName = node.getDisplayName();
        String[] nameParts = path.split("/");
        nameParts[nameParts.length - 1] = displayName;
        StringBuilder newName = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++)
        {
            if (i != 0)
            {
                newName.append(" > ");
            }
            newName.append(nameParts[i]);
        }
        return newName.toString();
    }
}
