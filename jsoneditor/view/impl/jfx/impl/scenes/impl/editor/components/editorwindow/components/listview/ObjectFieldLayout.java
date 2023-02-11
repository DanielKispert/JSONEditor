package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jsoneditor.controller.Controller;
import jsoneditor.model.json.JsonNodeWithPath;

public class ObjectFieldLayout extends HBox
{
    private final Controller controller;
    
    public ObjectFieldLayout(Controller controller, JsonNodeWithPath node)
    {
        this.controller = controller;
        VBox keyField = JsonEditorListView.makeFieldWithTitle("Key", node.getDisplayName());
        getChildren().add(keyField);
        JsonNode value = node.getNode();
        if (value.isArray() || value.isObject())
        {
            getChildren().add(makeGoToButton(new JsonNodeWithPath(value, node.getPath() + "/" + node.getDisplayName())));
        }
        else
        {
            getChildren().add(JsonEditorListView.makeFieldWithTitle("Value", value.asText()));
        }
        
    }
    
    private Button makeGoToButton(JsonNodeWithPath node)
    {
        Button goToButton = new Button();
        goToButton.setMaxWidth(Double.MAX_VALUE);
        goToButton.setMaxHeight(Double.MAX_VALUE);
        goToButton.setAlignment(Pos.BOTTOM_CENTER);
        HBox.setHgrow(goToButton, Priority.ALWAYS);
        VBox.setVgrow(goToButton, Priority.ALWAYS);
        goToButton.setText("Go to");
        goToButton.setContentDisplay(ContentDisplay.CENTER);
        goToButton.setOnAction(event -> controller.chooseNodeFromNavbar(node));
        return goToButton;
    }
}
