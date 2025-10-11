package com.daniel.jsoneditor.view.impl.jfx.buttons;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class CreateNewReferenceableObjectButton extends Button
{
    /**
     * Creates a button with a custom action handler.
     * @param handler the action to run when button is pressed
     */
    public CreateNewReferenceableObjectButton(final EventHandler<ActionEvent> handler)
    {
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_create_white_24dp.png");
        setOnAction(handler);
    }
}
