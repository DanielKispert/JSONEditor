package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.AutoAdjustingSplitPane;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.TableViewWithCompactNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.application.Platform;
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
        TableViewWithCompactNamebar parentView = (TableViewWithCompactNamebar) tableView.getParent();
        boolean clickCollapses = !parentView.isCollapsed();
        ButtonHelper.setButtonImage(this, clickCollapses ? "/icons/material/darkmode/outline_expand_white_24dp.png" : "/icons/material/darkmode/outline_collapse_white_24dp.png");
        AutoAdjustingSplitPane tableContainer = window.getTablesSplitPane();
        
        if (clickCollapses)
        {
            //the next click collapses the table, so we want to collapse to the bottom
            // Find the index of the parentView in the SplitPane
            int dividerAbove = tableContainer.getItems().indexOf(parentView) - 1;
            if (dividerAbove != -1 && dividerAbove < tableContainer.getDividers().size())
            {
                // Set the position of the divider to 1, effectively collapsing the parentView
                tableContainer.setDividerPosition(dividerAbove, 1.0);
            }
            parentView.collapse();
            // check the item above, and if it is collapsed, move its divider down, too
            // repeat this with the next region and so on and so on, until one is not collapsed
            int currentIndex = --dividerAbove;
            while (currentIndex >= 0)
            {
                Region itemAbove = (Region) tableContainer.getItems().get(currentIndex + 1);
                if (itemAbove instanceof TableViewWithCompactNamebar)
                {
                    TableViewWithCompactNamebar viewAbove = (TableViewWithCompactNamebar) itemAbove;
                    if (viewAbove.isCollapsed())
                    {
                        final int index = currentIndex;
                        // set the table container to expanded, then collapse it after moving to not trigger the listener
                        viewAbove.expand();
                        tableContainer.setDividerPosition(index, 1.0);
                        viewAbove.collapse();
                        
                    }
                    else
                    {
                        // If the item above is not collapsed, stop the loop
                        break;
                    }
                }
                currentIndex--;
            }
        }
        else
        {
            parentView.expand();
            // Find the index of the parentView in the SplitPane
            int dividerAbove = tableContainer.getItems().indexOf(parentView) - 1;
            if (dividerAbove != -1 && dividerAbove < tableContainer.getDividers().size())
            {
                // Set the position of the divider to 0.5, effectively expanding the parentView
                tableContainer.setDividerPosition(dividerAbove, 0.5);
            }
        }
    }
}
