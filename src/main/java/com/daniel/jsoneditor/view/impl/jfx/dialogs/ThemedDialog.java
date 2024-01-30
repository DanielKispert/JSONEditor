package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import javafx.scene.control.Dialog;


public abstract class ThemedDialog<T> extends Dialog<T>
{
    public ThemedDialog()
    {
        super();
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
    }
}
