package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.impl.NodeSearcher;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;

import java.util.ArrayList;
import java.util.List;

public class JsonEditorListView extends ListView<JsonNodeWithPath>
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private JsonNodeWithPath selection;
    
    public JsonEditorListView(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.manager = manager;
        this.model = model;
        setCellFactory(jsonNodeWithPathListView -> new JsonEditorListCell(this, model, controller));
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        this.selection = nodeWithPath;
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath());
        List<JsonNodeWithPath> childNodes = new ArrayList<>(); //either a list of array items or object fields
        if (node.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                childNodes.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (nodeWithPath.isObject())
        {
            childNodes = NodeSearcher.getAllChildNodesFromSchema(model.getRootSchema(), nodeWithPath, schema);
        }
        getItems().setAll(childNodes);
    }
    
    public JsonNodeWithPath getSelection()
    {
        return selection;
    }
    
    public EditorWindowManager getManager()
    {
        return manager;
    }
    
    
    public static VBox makeFieldWithTitle(String title, String value)
    {
        VBox fieldBox = new VBox();
        Label fieldTitle = new Label(title);
        fieldTitle.setTextFill(Color.GREY);
        fieldTitle.setFont(Font.font(null, FontWeight.NORMAL, 12));
        Label fieldValue = new Label(value);
        fieldValue.setTextFill(Color.BLACK);
        fieldValue.setFont(Font.font(null, FontWeight.NORMAL, 16));
        fieldBox.getChildren().addAll(fieldTitle, fieldValue);
        HBox.setHgrow(fieldTitle, Priority.ALWAYS);
        HBox.setHgrow(fieldValue, Priority.ALWAYS);
        HBox.setHgrow(fieldBox, Priority.ALWAYS);
        return fieldBox;
    }
}