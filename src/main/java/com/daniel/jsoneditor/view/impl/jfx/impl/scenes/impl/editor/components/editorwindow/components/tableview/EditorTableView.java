package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.control.TableView;


public abstract class EditorTableView extends TableView<JsonNodeWithPath>
{
    /**
     * selects a parent element. The table view will display a list of children (or items), depending on if the node is an object or an array
     * @param selection
     */
    public abstract void setSelection(JsonNodeWithPath selection);
    
    public abstract void focusItem(String itemPath);
}
