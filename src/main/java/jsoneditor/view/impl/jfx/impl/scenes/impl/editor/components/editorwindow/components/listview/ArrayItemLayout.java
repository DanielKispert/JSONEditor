package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.impl.NodeSearcher;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.field.EditorTextFieldFactory;

import java.util.Iterator;
import java.util.Map;

public class ArrayItemLayout extends HBox
{
    private final Controller controller;
    
    public ArrayItemLayout(ReadableModel model, Controller controller, JsonNodeWithPath item)
    {
        this.controller = controller;
        JsonNode schemaOfItem = model.getSubschemaForPath(item.getPath());
        if (item.isObject())
        {
            for (JsonNodeWithPath child : NodeSearcher.getAllChildNodesFromSchema(model.getRootSchema(), item, schemaOfItem))
            {
                if (!child.isObject() && !child.isArray())
                {
                    getChildren().add(EditorTextFieldFactory.makeTextField((ObjectNode) item.getNode(), child.getDisplayName(), child.getNode()));
                }
                
        
            }
            Iterator<Map.Entry<String, JsonNode>> fields = item.getNode().fields();
            while (fields.hasNext())
            {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();

            }
        }
        getChildren().add(makeRemoveButton(item));
    }
    
    private Button makeRemoveButton(JsonNodeWithPath node)
    {
        Button removeButton = new Button("X");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(event -> controller.removeNode(node.getPath()));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    
}
