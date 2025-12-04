package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.diff.DiffEntry;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.DiffDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.util.List;


/**
 * Button to show differences between the JSON in editor and the JSON saved on disk.
 * Opens a dialog showing added, removed, and modified nodes with special markers for references.
 */
public class DiffButton extends Button
{
    private final Controller controller;
    private final EditorWindowManager manager;
    private final Stage parentStage;
    
    public DiffButton(Controller controller, EditorWindowManager manager, Stage parentStage)
    {
        super();
        this.controller = controller;
        this.manager = manager;
        this.parentStage = parentStage;
        
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_difference_white_24dp.png");
        setOnAction(actionEvent -> showDiff());
        setTooltip(new Tooltip("Compare editor with JSON on disk"));
    }
    
    private void showDiff()
    {
        final List<DiffEntry> diffs = controller.calculateJsonDiff();
        
        if (diffs == null || diffs.isEmpty())
        {
            manager.showToast(Toasts.NO_DIFFERENCES_TOAST);
            return;
        }
        
        final DiffDialog dialog = new DiffDialog(diffs, manager, controller);
        dialog.initOwner(parentStage);
        dialog.showAndWait();
    }
}

