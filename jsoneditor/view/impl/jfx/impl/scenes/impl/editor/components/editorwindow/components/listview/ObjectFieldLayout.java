package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jsoneditor.controller.Controller;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.field.EditorTextFieldFactory;

public class ObjectFieldLayout extends HBox
{
    private final Controller controller;
    
    public ObjectFieldLayout(JsonNode parent, Controller controller, JsonNodeWithPath node)
    {
        this.controller = controller;
        setAlignment(Pos.CENTER_LEFT);
        VBox keyField = JsonEditorListView.makeFieldWithTitle("Key", node.getDisplayName());
        getChildren().add(keyField);
        JsonNode value = node.getNode();
        if (value.isArray() || value.isObject())
        {
            getChildren().add(makeGoToButton(node));
        }
        else
        {
            getChildren().add(EditorTextFieldFactory.makeTextField((ObjectNode) parent, node.getDisplayName(), value));
        }
        
    }
    
    private Button makeGoToButton(JsonNodeWithPath node)
    {
        Button goToButton = new Button();
        goToButton.setMaxWidth(Double.MAX_VALUE);
        goToButton.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(goToButton, Priority.ALWAYS);
        VBox.setVgrow(goToButton, Priority.ALWAYS);
        goToButton.setText("Go to");
        goToButton.setContentDisplay(ContentDisplay.CENTER);
        goToButton.setOnAction(event -> controller.chooseNodeFromNavbar(node.getPath()));
        return goToButton;
    }
}
