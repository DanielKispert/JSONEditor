package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.node.MissingNode;
import javafx.scene.control.TreeItem;

/**
 * Placeholder tree item shown when a large array is collapsed in the navbar.
 * Double-clicking it expands the parent array to show all children.
 */
public class NavbarPlaceholderItem extends TreeItem<JsonNodeWithPath>
{
    private final int hiddenCount;
    private final String parentArrayPath;

    public NavbarPlaceholderItem(int hiddenCount, String parentArrayPath)
    {
        super(new JsonNodeWithPath(MissingNode.getInstance(), parentArrayPath));
        this.hiddenCount = hiddenCount;
        this.parentArrayPath = parentArrayPath;
    }

    public String getParentArrayPath()
    {
        return parentArrayPath;
    }

    public String getDisplayText()
    {
        return "▶ Show all (" + hiddenCount + " hidden)";
    }
}

