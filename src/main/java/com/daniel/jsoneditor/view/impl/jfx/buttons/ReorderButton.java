package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ReorderArrayDialog;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Button;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


public class ReorderButton extends Button
{
    private final Controller controller;
    private final ReadableModel model;
    private final Supplier<String> selectedPathSupplier;
    
    public ReorderButton(ReadableModel model, Controller controller, Supplier<String> selectedPathSupplier)
    {
        super();
        this.model = model;
        this.controller = controller;
        this.selectedPathSupplier = selectedPathSupplier;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_reorder_white_24dp.png");
        setOnAction(actionEvent -> openReorderDialog());
    }
    
    private void openReorderDialog()
    {
        final String path = selectedPathSupplier.get();
        if (path == null)
        {
            return;
        }
        
        final JsonNode arrayNode = model.getNodeForPath(path).getNode();
        if (arrayNode == null || !arrayNode.isArray())
        {
            return;
        }
        
        final ReorderArrayDialog dialog = new ReorderArrayDialog(arrayNode);
        final Optional<List<Integer>> result = dialog.showAndWait();
        result.ifPresent(newIndices -> controller.reorderArray(path, newIndices));
    }
}
