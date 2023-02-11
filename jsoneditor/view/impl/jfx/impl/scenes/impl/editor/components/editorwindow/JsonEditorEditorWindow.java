package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.JsonEditorListView;

/*
 * Editor consists of a vbox holding a label and a listview
 *
 */
public class JsonEditorEditorWindow extends VBox
{
    private final JsonEditorNamebar nameBar;
    
    private final JsonEditorListView editor;
    
    public JsonEditorEditorWindow(ReadableModel model, Controller controller)
    {
        nameBar = new JsonEditorNamebar();
        editor = new JsonEditorListView(model, controller);
        VBox.setVgrow(editor, Priority.ALWAYS);
        updateSelectedJson(model);
        getChildren().addAll(nameBar, editor);
    }
    
    
    public void updateSelectedJson(ReadableModel model)
    {
        JsonNodeWithPath nodeWithPath = model.getSelectedJsonNode();
        JsonNode selectedNode = nodeWithPath.getNode();
        if (selectedNode != null)
        {
            // update name bar
            nameBar.setSelection(nodeWithPath);
            // update editing window
            editor.setSelection(nodeWithPath);
        }
    }
    

}
