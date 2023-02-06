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
        Button removeSelectedObjectButton = new Button("Remove selected object");
        removeSelectedObjectButton.setOnAction(event ->
        {
            UIHandlerImpl.showConfirmDialog(controller::removeSelectedNode, "this will remove the json object currently visible in the editor window");
        });
        getItems().addAll(removeSelectedObjectButton);
    }
}
