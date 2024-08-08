package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;


public class EditorTableRow extends TableRow<JsonNodeWithPath>
{
    private final ReadableModel model;
    
    private final Controller controller;
    
    public EditorTableRow(ReadableModel model, Controller controller)
    {
        super();
        this.model = model;
        this.controller = controller;
        setOnDragDetected(this::handleDragDetected);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
    }
    

    
    private void handleDragDetected(javafx.scene.input.MouseEvent event)
    {
        if (!isEmpty())
        {
            int draggedIndex = getIndex();
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(draggedIndex));
            db.setContent(content);
            event.consume();
        }
    }
    
    private void handleDragOver(javafx.scene.input.DragEvent event)
    {
        if (event.getGestureSource() != this && event.getDragboard().hasString())
        {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }
    
    private void handleDragDropped(javafx.scene.input.DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasString())
        {
            TableView<JsonNodeWithPath> tableView = getTableView();
            int draggedIndex = Integer.parseInt(db.getString());
            int dropIndex = getIndex();
            
            if (draggedIndex != dropIndex)
            {
                JsonNodeWithPath draggedItem = tableView.getItems().get(draggedIndex);
                controller.moveItemToIndex(null, draggedItem,
                        dropIndex); // Assuming newParent is null for reordering within the same parent
                tableView.getSelectionModel().select(dropIndex);
            }
            event.setDropCompleted(true);
        }
        else
        {
            event.setDropCompleted(false);
        }
        event.consume();
    }
    
    
}
