package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.List;
import java.util.Optional;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ShowUsagesDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import javafx.scene.control.Button;


public class ShowUsagesButton extends Button
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private JsonNodeWithPath selection;
    
    public ShowUsagesButton(ReadableModel model, EditorWindowManager manager)
    {
        super("🔍"); // heck yeah, unicode magnifying glass so I don't need icons
        this.model = model;
        this.manager = manager;
        setOnAction(actionEvent -> handleClick());
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        this.selection = selection;
        String path = selection.getPath();
        ReferenceableObject object = model.getReferenceableObject(path);
        this.setVisible(object != null);
        this.setManaged(object != null);
    }
    
    private void handleClick()
    {
        List<String> usagesOfThisReferencedObjectInstance = model.getReferencesToObjectForPath(selection.getPath());
        ShowUsagesDialog dialog = new ShowUsagesDialog(null, selection);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (manager.canAnotherWindowBeAdded())
            {
                manager.selectInNewWindow(s);
            }
            else
            {
                manager.selectFromNavbar(s);
            }
            
        });
    
    
    
    }
}
