package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import jsoneditor.controller.Controller;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.Iterator;
import java.util.Map;

public class ArrayItemLayout extends HBox
{
    private final Controller controller;
    
    public ArrayItemLayout(Controller controller, JsonNodeWithPath item)
    {
        this.controller = controller;
        Iterator<Map.Entry<String, JsonNode>> fields = item.getNode().fields();
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            if (value.getNodeType() != JsonNodeType.OBJECT && value.getNodeType() != JsonNodeType.ARRAY)
            {
                getChildren().add(JsonEditorListView.makeFieldWithTitle(key, value.asText()));
            }
        }
        getChildren().add(makeRemoveButton(item));
    }
    
    private Button makeRemoveButton(JsonNodeWithPath node)
    {
        Button removeButton = new Button("X");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(event -> controller.removeNodeFromArray(node.getNode()));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    
}
