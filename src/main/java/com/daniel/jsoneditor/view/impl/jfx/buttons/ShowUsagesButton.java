package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.Optional;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ShowUsagesDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;


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
        ShowUsagesDialog dialog = new ShowUsagesDialog(model.getReferencesToObjectForPath(selection.getPath()), selection);
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
