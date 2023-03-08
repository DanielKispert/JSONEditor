package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.layout.HBox;
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
    
    private String selectedPath;
    
    private final JsonEditorNamebar nameBar;
    
    private final JsonEditorListView editor;
    
    private final ReadableModel model;
    
    public JsonEditorEditorWindow(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.model = model;
        nameBar = new JsonEditorNamebar();
        editor = new JsonEditorListView(manager, model, controller);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().addAll(nameBar, editor);
    }
    
    public void setSelectedPath(String path)
    {
        this.selectedPath = path;
        JsonNodeWithPath newNode = model.getNodeForPath(path);
        nameBar.setSelection(newNode);
        editor.setSelection(newNode);
    }
    
    public String getSelectedPath()
    {
        return selectedPath;
    }
}
