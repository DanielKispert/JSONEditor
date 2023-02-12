package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonEditorListView extends ListView<JsonNodeWithPath>
{
    public JsonEditorListView(ReadableModel model, Controller controller)
    {
        setCellFactory(jsonNodeWithPathListView -> new JsonEditorListCell(model, controller));
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        JsonNode node = nodeWithPath.getNode();
        List<JsonNodeWithPath> childNodes = new ArrayList<>(); //either a list of array items or object fields
        if (node.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                childNodes.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (node.isObject())
        {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); )
            {
                Map.Entry<String, JsonNode> field = it.next();
                childNodes.add(new JsonNodeWithPath(field.getValue(), nodeWithPath.getPath() + "/" + field.getKey()));
            }
        }
        getItems().setAll(childNodes);
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
        HBox.setHgrow(fieldBox, Priority.ALWAYS);
        return fieldBox;
    }
}
