package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.view.impl.jfx.buttons.DeleteAllButton;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Supplier;

public class TableViewButtonBar extends HBox
{
    private final Button addItemButton;
    private final DeleteAllButton deleteAllButton;
    
    public TableViewButtonBar(Controller controller, Supplier<List<String>> pathsSupplier, Supplier<String> selectedPathSupplier)
    {
        addItemButton = new Button("Add Item");
        addItemButton.setOnAction(event -> controller.addNewNodeToArray(selectedPathSupplier.get()));
        
        deleteAllButton = new DeleteAllButton(controller, pathsSupplier);
        
        addItemButton.setMaxWidth(Double.MAX_VALUE);
        deleteAllButton.setMaxWidth(Double.MAX_VALUE);
        
        getChildren().addAll(addItemButton, deleteAllButton);
    }
    
    public void updateBottomBar(boolean showAddItemButton, boolean showDeleteAllButton)
    {
        addItemButton.setVisible(showAddItemButton);
        deleteAllButton.setVisible(showDeleteAllButton);
    }
    
}
