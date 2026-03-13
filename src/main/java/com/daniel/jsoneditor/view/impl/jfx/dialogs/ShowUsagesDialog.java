package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.util.List;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.listview.DialogWithListView;


/**
 * Shows where a ReferenceableObject is used and lets the user open a usage in the current or a new window.
 */
public class ShowUsagesDialog extends DialogWithListView<ReferenceToObjectInstance, FindResult>
{
    
    public ShowUsagesDialog(List<ReferenceToObjectInstance> items, JsonNodeWithPath jsonNodeWithPath,
            SettingsController settingsController)
    {
        super(items);
        this.setTitle("Usages of " + jsonNodeWithPath.getDisplayName());
        
        listView.getSelectionModel().selectFirst();
        getDialogPane().setContent(listView);
        addOpenInNewWindowCheckBox(settingsController);
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
    
    @Override
    protected FindResult convertSelectedItem(ReferenceToObjectInstance selectedItem)
    {
        return new FindResult(selectedItem.getPath(), isOpenInNewWindowRequested());
    }
}
