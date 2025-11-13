package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Button to bookmark/unbookmark a history entry
 */
public class HistoryBookmarkButton extends Button
{
    private final CommandManager.HistoryEntry historyEntry;
    private final Runnable onToggle;
    
    public HistoryBookmarkButton(CommandManager.HistoryEntry historyEntry, Runnable onToggle)
    {
        super();
        this.historyEntry = historyEntry;
        this.onToggle = onToggle;
        
        updateIcon();
        setOnAction(actionEvent -> handleToggleBookmark());
    }
    
    private void handleToggleBookmark()
    {
        historyEntry.setBookmarked(!historyEntry.isBookmarked());
        updateIcon();
        if (onToggle != null)
        {
            onToggle.run();
        }
    }
    
    public void updateIcon()
    {
        if (historyEntry.isBookmarked())
        {
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_added_bookmark_white_24dp.png");
            setTooltip(new Tooltip("Remove bookmark"));
        }
        else
        {
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_add_bookmark_white_24dp.png");
            setTooltip(new Tooltip("Bookmark this version"));
        }
    }
}
