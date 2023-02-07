package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.view.impl.jfx.impl.UIHandlerImpl;

public class JsonEditorToolbar extends ToolBar
{
    
    private final Controller controller;
    
    public JsonEditorToolbar(ReadableModel model, Controller controller)
    {
        this.controller = controller;
        Button removeSelectedObjectButton = new Button("Remove visible object");
        removeSelectedObjectButton.setOnAction(event ->
        {
            UIHandlerImpl.showConfirmDialog(controller::removeSelectedNode, "this will remove the json object currently visible in the editor window, not just the selected one!");
        });
        Button saveButton = new Button("Save to file");
        saveButton.setOnAction(event -> controller.saveToFile());
        getItems().addAll(saveButton, removeSelectedObjectButton);
    }
}
