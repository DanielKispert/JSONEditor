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
        
        listView.getSelectionModel().selectFirst();
        getDialogPane().setContent(listView);
    }
    
    @Override
    protected String getOkButtonText()
    {
        return "Open";
    }
    
    @Override
    protected void onListItemDoubleClick(ReferenceToObjectInstance item)
    {
        handleDialogOk();
    }
}
