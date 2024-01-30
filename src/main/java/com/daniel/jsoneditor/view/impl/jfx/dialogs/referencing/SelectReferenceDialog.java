package com.daniel.jsoneditor.view.impl.jfx.dialogs.referencing;

import java.util.List;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.FindDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;


public class SelectReferenceDialog extends FindDialog
{
    private ButtonType createButtonType;
    
    public SelectReferenceDialog(List<ReferenceableObjectInstance> suggestions)
    {
        super(suggestions);
        this.setTitle("Select Object to reference");
    }
    
    @Override
    protected void initializeListView(List<ReferenceableObjectInstance> items)
    {
        super.initializeListView(items);
        
        createButtonType = new ButtonType("Create new reference", ButtonData.OTHER);
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().add(createButtonType);
    }
    
    @Override
    protected String convertResult(ButtonType buttonType)
    {
        if (buttonType == createButtonType)
        {
            return ReferenceType.CREATE_NEW_REFERENCE.name();
        }
        else
        {
            return super.convertResult(buttonType);
        }
    }
    
    @Override
    protected void onListItemDoubleClick(ReferenceableObjectInstance item)
    {
    }
}
