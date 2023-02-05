package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.Iterator;
import java.util.Map;

public class JsonEditorNavbar extends VBox
{
    private final ReadableModel model;
    
    private final Controller controller;
    
    public JsonEditorNavbar(ReadableModel model, Controller controller)
    {
        this.model = model;
        this.controller = controller;
        SplitPane.setResizableWithParent(this, false);
        addChildrenToNavbar(model.getRootJson(), 1);
    }
    
    private void addChildrenToNavbar(JsonNode node, int depth)
    {
        if (depth == 1)
        {
            Label navbarLabel = new Label("Root Element");
            navbarLabel.setOnMouseClicked(event -> handleNavbarClick("Root Element", node));
            getChildren().add(navbarLabel);
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            String indentation = "    ".repeat(Math.max(0, depth));
            if (value.getNodeType() == JsonNodeType.OBJECT || value.getNodeType() == JsonNodeType.ARRAY)
            {
                Label label = new Label(indentation + key);
                label.setOnMouseClicked(event -> handleNavbarClick(key, value));
                getChildren().add(label);
                addChildrenToNavbar(value, depth + 1);
            }
        }
    }
    
    private void handleNavbarClick(JsonNode node, String name, String path)
    {
        JsonNodeWithPath nodeWithPath = new JsonNodeWithPath(node, name, path);
        controller.chooseNodeFromNavbar(nodeWithPath);
    }
}
