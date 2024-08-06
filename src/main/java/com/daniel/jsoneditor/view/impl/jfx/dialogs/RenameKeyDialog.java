package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public class RenameKeyDialog extends ThemedDialog<String>
{
    private final TextField keyField;
    
    public RenameKeyDialog(String prefill)
    {
        super();
        setTitle("Rename Key");
        
        keyField = new TextField(prefill);
        
        VBox vbox = new VBox();
        vbox.getChildren().add(keyField);
        getDialogPane().setContent(vbox);
        
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        
        setResultConverter(button -> {
            if (button == ButtonType.OK)
            {
                return keyField.getText();
            }
            return null;
        });
    }
}
