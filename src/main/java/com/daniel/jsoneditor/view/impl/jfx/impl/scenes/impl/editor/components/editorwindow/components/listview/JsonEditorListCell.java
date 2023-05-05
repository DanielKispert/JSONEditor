package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.impl.EditorTableViewImpl;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class JsonEditorListCell extends ListCell<JsonNodeWithPath>
{
    private boolean draggable;
    private final Controller controller;
    
    private final ReadableModel model;
    
    private final EditorTableViewImpl parent;
    
    public JsonEditorListCell(EditorTableViewImpl parent, ReadableModel model, Controller controller)
    {
        this.parent = parent;
        this.model = model;
        this.controller = controller;
    }
    
    @Override
    protected void updateItem(JsonNodeWithPath item, boolean empty)
    {
        JsonNodeWithPath selectedNode = parent.getSelection();
    
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            this.draggable = false;
            setGraphic(null);
        }
        else
        {
            if (selectedNode.isArray())
            {
                this.draggable = true;
                setGraphic(new ArrayItemLayout(model, controller, item));
            }
            else if (selectedNode.isObject())
            {
                this.draggable = false;
                setGraphic(new ObjectFieldLayout(selectedNode.getNode(), parent.getManager(), item));
            }
            else
            {
                this.draggable = false;
                setGraphic(null);
            }
        }
        setDragBehavior();
    }
    
    private void setDragBehavior()
    {
        setOnDragDetected(event ->
        {
            if (!draggable)
            {
                return;
            }
            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(DataFormat.PLAIN_TEXT, getItem().toString());
            dragboard.setContent(content);
            event.consume();
        });
    
        setOnDragOver(event ->
        {
            if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        
            event.consume();
        });
    
        setOnDragEntered(event ->
        {
            if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
            {
                setOpacity(0.3);
            }
        });
    
        setOnDragExited(event ->
        {
            if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
            {
                setOpacity(1);
            }
        });
    
        setOnDragDropped(event ->
        {
            if (getItem() == null)
            {
                return;
            }
        
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString())
            {
                JsonNodeWithPath itemToMove = getItem();
                int newIndex = getListView().getSelectionModel().getSelectedIndex();
                success = true;
                controller.moveItemToIndex(parent.getSelection(), itemToMove, newIndex);
            }
            event.setDropCompleted(success);
    
            event.consume();
        });
        setOnDragDone(DragEvent::consume);
    
    }
}

