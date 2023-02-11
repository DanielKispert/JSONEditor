package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;


import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

public class JsonEditorListCell extends ListCell<JsonNodeWithPath>
{
    private boolean draggable;
    private final Controller controller;
    
    private final ReadableModel model;
    
    public JsonEditorListCell(ReadableModel model, Controller controller)
    {
        this.model = model;
        this.controller = controller;
       
    }
    
    @Override
    protected void updateItem(JsonNodeWithPath item, boolean empty)
    {
    
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            this.draggable = false;
            setGraphic(null);
        }
        else
        {
            if (model.editingAnArray())
            {
                this.draggable = true;
                setGraphic(new ArrayItemLayout(controller, item));
            }
            else if (model.editingAnObject())
            {
                this.draggable = false;
                setGraphic(new ObjectFieldLayout(controller, item));
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
                int newIndex = getListView().getSelectionModel().getSelectedIndex();
                getListView().getItems().remove(getItem());
                getListView().getItems().add(newIndex, getItem());
                getListView().getSelectionModel().clearAndSelect(newIndex);
                success = true;
            }
            event.setDropCompleted(success);
        
            event.consume();
        });
        setOnDragDone(DragEvent::consume);
    
    }
}

