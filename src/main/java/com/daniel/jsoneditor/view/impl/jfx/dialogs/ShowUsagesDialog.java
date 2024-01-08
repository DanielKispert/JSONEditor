package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.util.List;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.listview.DialogWithListView;


/**
 * this dialog shows where a ReferenceableObject is used
 */
public class ShowUsagesDialog extends DialogWithListView<ReferenceToObjectInstance>
{
    
    public ShowUsagesDialog(List<ReferenceToObjectInstance> items, JsonNodeWithPath jsonNodeWithPath)
    {
        super(items);
        this.setTitle("Usages of " + jsonNodeWithPath.getDisplayName());
        
        setResultConverter(this::convertResult);
        
        listView.getSelectionModel().selectFirst();
        getDialogPane().setContent(listView);
        getDialogPane().setPrefWidth(500);
        getDialogPane().setPrefHeight(400);
    }
    
    @Override
    protected String getOkButtonText()
    {
        return "Open";
    }
    
    @Override
    protected void onListItemDoubleClick(ReferenceToObjectInstance item)
    {
    
    }
}
