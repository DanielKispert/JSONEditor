package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;


public class EditorTableRow extends TableRow<JsonNodeWithPath>
{
    
    public EditorTableRow(Controller controller)
    {
        super();
        
        setOnDragDetected(event -> {
            if (!isEmpty())
            {
                int draggedIndex = getIndex();
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(draggedIndex));
                db.setContent(content);
                event.consume();
            }
        });
        
        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        setOnDragDropped(event -> {
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
        });
    }
    
    
}
