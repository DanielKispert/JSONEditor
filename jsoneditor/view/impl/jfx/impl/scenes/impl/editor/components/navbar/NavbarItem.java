package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import javafx.scene.control.TreeItem;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

public class NavbarItem extends TreeItem<JsonNodeWithPath>
{
    
    public NavbarItem(ReadableModel model, String path)
    {
        super(model.getNodeForPath(path));
    }
    
}
