package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import javafx.scene.control.ButtonType;


public class AreYouSureDialog extends ThemedAlert
{
    public AreYouSureDialog(String title, String headerText, String contentText)
    {
        super(AlertType.CONFIRMATION);
        setTitle(title);
        setHeaderText(headerText);
        setContentText(contentText);
        getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
    }
}
