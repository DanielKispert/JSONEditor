package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.List;
import java.util.Optional;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ShowUsagesDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;


public class ShowUsagesButton extends Button
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private JsonNodeWithPath selection;
    
    public ShowUsagesButton(ReadableModel model, EditorWindowManager manager)
    {
        super();
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_pageview_white_24dp.png");
        this.model = model;
        this.manager = manager;
        setOnAction(actionEvent -> handleClick());
        setTooltip(new Tooltip("Show usages"));
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        this.selection = selection;
        String path = selection.getPath();
        boolean visible = model.getReferenceableObject(path) != null && !selection.isArray();
        this.setVisible(visible);
        this.setManaged(visible);
    }
    
    private void handleClick()
    {
        List<ReferenceToObjectInstance> references = model.getReferencesToObjectForPath(selection.getPath());
        if (references.isEmpty())
        {
            manager.showToast(Toasts.NO_REFERENCES_TOAST);
            return;
        }
        ShowUsagesDialog dialog = new ShowUsagesDialog(references, selection);
        Optional<String> result = dialog.showAndWait(); //the result is the path of the node to open
        result.ifPresent(s -> {
            if (manager.canAnotherWindowBeAdded())
            {
                manager.selectInNewWindow(s);
            }
            else
            {
                manager.selectInFirstWindow(s);
            }
            
        });
    }
}
