package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class ThemedAlert extends Alert
{
    public ThemedAlert(AlertType alertType)
    {
        super(alertType);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
    }
    
    public ThemedAlert(AlertType alertType, String s, ButtonType... buttonTypes)
    {
        super(alertType, s, buttonTypes);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
    }
}
