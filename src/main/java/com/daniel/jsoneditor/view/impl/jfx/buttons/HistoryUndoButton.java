package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Button to undo a single history entry
 */
public class HistoryUndoButton extends Button
{
    public HistoryUndoButton(CommandManager.HistoryEntry historyEntry)
    {
        super();
        
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_undo_white_24dp.png");
        setTooltip(new Tooltip("Undo: " + historyEntry.getCommand().getLabel()));
        
        // Action is handled by the parent popup
    }
}

