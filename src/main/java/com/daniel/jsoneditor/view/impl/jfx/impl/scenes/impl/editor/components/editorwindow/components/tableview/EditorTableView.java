package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.List;


public abstract class EditorTableView extends TableView<JsonNodeWithPath>
{
    /**
     * selects a parent element. The table view will display a list of children (or items), depending on if the node is an object or an array
     * @param selection
     */
    public abstract void setSelection(JsonNodeWithPath selection);
    
    public abstract void focusItem(String itemPath);
    
    public abstract String getSelectedPath();
    
    public abstract void filter();
    
    public abstract ObservableList<JsonNodeWithPath> getUnfilteredItems();
    
    /**
     * @return a list of the paths of each row item that is currently displayed (after filters)
     */
    public abstract List<String> getCurrentlyDisplayedPaths();
    
    // Granular update methods for specific model changes
    public abstract void handleItemAdded(String path);
    
    public abstract void handleItemRemoved(String path);
    
    public abstract void handleItemChanged(String path);
    
    public abstract void handleItemMoved(String path);
    
    public abstract void handleSorted(String path);
}
