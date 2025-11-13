package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Button to revert to a specific history entry
 */
public class HistoryRevertButton extends Button
{
    public HistoryRevertButton(CommandManager.HistoryEntry historyEntry)
    {
        super();
        
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_history_white_24dp.png");
        setTooltip(new Tooltip("Revert to: " + historyEntry.getCommand().getLabel()));
        
        // Action is handled by the parent popup
    }
}


