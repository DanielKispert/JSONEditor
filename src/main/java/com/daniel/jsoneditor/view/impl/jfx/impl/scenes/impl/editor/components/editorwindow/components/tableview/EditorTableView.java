package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.elements.Collapsible;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.List;


public abstract class EditorTableView extends TableView<JsonNodeWithPath> implements Collapsible
{
    /**
     * selects a parent element. The table view will display a list of children (or items), depending on if the node is an object or an array
     * @param selection
     */
    public abstract void setSelection(JsonNodeWithPath selection);
    
    public abstract void focusItem(String itemPath);
    
    @Override
    public void collapse()
    {
        // do nothing intentionally
    }
    
    @Override
    public void expand()
    {
        // do nothing intentionally
    }
    
    public abstract String getSelectedPath();
    
    public abstract void filter();
    
    @Override
    public boolean isCollapsed()
    {
        return false;
    }
    
    public abstract ObservableList<JsonNodeWithPath> getUnfilteredItems();
    
    /**
     * @return a list of the paths of each row item that is currently displayed (after filters)
     */
    public abstract List<String> getCurrentlyDisplayedPaths();
}
