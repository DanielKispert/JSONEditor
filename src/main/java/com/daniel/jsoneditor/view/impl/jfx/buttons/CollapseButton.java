package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.TableViewWithCompactNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;

public class CollapseButton extends Button
{
    private final EditorTableView tableView;
    
    private final JsonEditorEditorWindow window;
    
    public CollapseButton(EditorTableView tableView, JsonEditorEditorWindow window)
    {
        super();
        this.tableView = tableView;
        this.window = window;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_collapse_white_24dp.png");
        setOnAction(actionEvent -> handleClick());
    }
    
    private void handleClick()
    {
        boolean clickCollapses = tableView.isVisible();
        tableView.setVisible(!clickCollapses);
        ButtonHelper.setButtonImage(this, clickCollapses ? "/icons/material/darkmode/outline_expand_white_24dp.png" : "/icons/material/darkmode/outline_collapse_white_24dp.png");
        SplitPane tableContainer = window.getTablesSplitPane();
        TableViewWithCompactNamebar parentView = (TableViewWithCompactNamebar) tableView.getParent();
        
        if (clickCollapses)
        {
            //the next click collapses the table, so we want to collapse to the bottom
            parentView.setPrefHeight(40);
            // Find the index of the parentView in the SplitPane
            int dividerIndex = tableContainer.getItems().indexOf(parentView) - 1;
            if (dividerIndex != -1 && dividerIndex < tableContainer.getDividers().size())
            {
                // Set the position of the divider to 1, effectively collapsing the parentView
                tableContainer.setDividerPosition(dividerIndex, 1.0);
            }
        }
        else
        {
            //the next click expands the table, so we want to expand to the original size
            parentView.setPrefHeight(Region.USE_COMPUTED_SIZE);
            // Find the index of the parentView in the SplitPane
            int dividerIndex = tableContainer.getItems().indexOf(parentView) - 1;
            if (dividerIndex != -1 && dividerIndex < tableContainer.getDividers().size())
            {
                // Set the position of the divider to 0.5, effectively expanding the parentView
                tableContainer.setDividerPosition(dividerIndex, 0.5);
            }
        }
    }
}
