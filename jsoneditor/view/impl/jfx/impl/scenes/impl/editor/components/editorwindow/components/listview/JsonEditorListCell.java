package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;


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
    
    private final JsonEditorListView parent;
    
    public JsonEditorListCell(JsonEditorListView parent, ReadableModel model, Controller controller)
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

