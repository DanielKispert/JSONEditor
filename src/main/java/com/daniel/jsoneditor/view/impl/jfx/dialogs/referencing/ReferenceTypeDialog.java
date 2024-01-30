package com.daniel.jsoneditor.view.impl.jfx.dialogs.referencing;

import com.daniel.jsoneditor.view.impl.jfx.dialogs.ThemedDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;


public class ReferenceTypeDialog extends ThemedDialog<ReferenceType>
{
    public ReferenceTypeDialog()
    {
        setTitle("Create Reference");
        setContentText("Choose reference type");
        setResizable(false);
        
        ButtonType objectReferenceButton = new ButtonType("Reference to Object");
        ButtonType manualReferenceButton = new ButtonType("Manual Reference");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        
        getDialogPane().getButtonTypes().addAll(objectReferenceButton, manualReferenceButton, buttonTypeCancel);
        
        setResultConverter(buttonType -> {
            if (buttonType == objectReferenceButton)
            {
                return ReferenceType.REFERENCE_TO_OBJECT;
            }
            else if (buttonType == manualReferenceButton)
            {
                return ReferenceType.MANUAL_REFERENCE;
            }
            return null;
        });
    }
}
