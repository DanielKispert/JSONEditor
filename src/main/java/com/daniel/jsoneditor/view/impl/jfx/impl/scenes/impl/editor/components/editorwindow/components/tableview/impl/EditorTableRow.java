package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;


public class EditorTableRow extends TableRow<JsonNodeWithPath>
{
    private final ReadableModel model;
    
    private final Controller controller;
    
    private final EditorTableView myTableView;
    
    public EditorTableRow(ReadableModel model, Controller controller, EditorTableView myTableView)
    {
        super();
        this.model = model;
        this.controller = controller;
        this.myTableView = myTableView;
        setOnDragDetected(this::handleDragDetected);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        setContextMenu(makeContextMenu());
    }
    
    private void handleKeyPressed(KeyEvent event)
    {
        if (isFocused())
        {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(event))
            {
                copy();
            }
            else if (new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN).match(event))
            {
                paste();
            }
        }
    }
    
    private ContextMenu makeContextMenu()
    {
        // Add context menu for right-click functionality
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
        
        copyItem.setOnAction(event -> copy());
        pasteItem.setOnAction(event -> paste());
        
        this.setOnContextMenuRequested(event -> copyItem.setVisible(getItem() != null));
        contextMenu.getItems().addAll(copyItem, pasteItem);
        return contextMenu;
    }
    
    private void copy()
    {
        JsonNodeWithPath item = getItem();
        controller.copyToClipboard(item != null ? item.getPath() : null);
    }
    
    private void paste()
    {
        JsonNodeWithPath selectedItem = getItem();
        if (selectedItem != null)
        {
            controller.pasteFromClipboardReplacingChild(selectedItem.getPath());
        }
        else
        {
            controller.pasteFromClipboardIntoParent(myTableView.getSelectedPath());
        }
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
