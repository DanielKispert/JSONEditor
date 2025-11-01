package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Button to toggle the visibility of empty columns in table views
 */
public class VisibilityToggleButton extends Button
{
    private final EditorTableView tableView;
    
    public VisibilityToggleButton(EditorTableView tableView)
    {
        super();
        this.tableView = tableView;
        updateIcon();
        setOnAction(event -> {
            tableView.toggleTemporaryShowAllColumns();
            updateIcon();
        });
    }
    
    private void updateIcon()
    {
        if (tableView.isTemporaryShowAllColumns())
        {
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_not_visible_white_24dp.png");
            setTooltip(new Tooltip("Show all columns"));
        }
        else
        {
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_visible_white_24dp.png");
            setTooltip(new Tooltip("Hide empty columns"));
        }
    }
}
