package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import javafx.scene.control.TreeItem;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;

public class NavbarItem extends TreeItem<JsonNodeWithPath>
{
    
    public NavbarItem(ReadableModel model, String path)
    {
        super(model.getNodeForPath(path));
    }
    
}
