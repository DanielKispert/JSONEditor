package jsoneditor.view.impl.jfx.impl.scenes.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;

import java.util.Iterator;
import java.util.Map;

public class MainEditor extends SceneHandlerImpl
{
    private ListView<HBox> editingWindow;
    
    private Label nameBar;
    
    
    public MainEditor(Controller controller, ReadableModel model)
    {
        super(controller, model);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        double startingSceneWidth = 800;
        double startingSceneHeight = 600;
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, startingSceneWidth, startingSceneHeight);
        
        // Toolbar
        ToolBar toolbar = new ToolBar();
        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");
        toolbar.getItems().addAll(button1, button2);
        
        root.setTop(toolbar);
        
        // Navigation bar
        VBox navbar = new VBox();
        addChildrenToNavbar(model.getRootJson(), navbar, 1);
        
        // Editing window
        editingWindow = new ListView<>();
        
        // name of current json
        nameBar = new Label("Root Element");
        updateSelectedJson();
        
        // layout for edit window and name
        VBox editorWithName = new VBox();
        editorWithName.getChildren().addAll(nameBar, editingWindow);
       
        
        // Split pane
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(navbar, editorWithName);
        splitPane.prefWidthProperty().bind(scene.widthProperty());
        splitPane.prefHeightProperty().bind(scene.heightProperty());
        
        root.setLeft(splitPane);
        
        
        return scene;
    }
    
    public void updateSelectedJson()
    {
        JsonNode selectedNode = model.getSelectedJsonNode();
        if (selectedNode != null)
        {
            // update name bar
            nameBar.setText(model.getNameOfSelectedJsonNode());
            // update editing window
            Iterator<Map.Entry<String, JsonNode>> fields = selectedNode.fields();
            editingWindow.getItems().clear();
            while (fields.hasNext())
            {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                if (value.getNodeType() != JsonNodeType.OBJECT && value.getNodeType() != JsonNodeType.ARRAY)
                {
                    HBox item = new HBox();
                    TextField keyField = new TextField(key);
                    keyField.setPrefWidth(editingWindow.getWidth() / 2);
                    TextField valueField = new TextField(value.asText());
                    valueField.setPrefWidth(editingWindow.getWidth() / 2);
                    item.getChildren().addAll(keyField, valueField);
                    editingWindow.getItems().add(item);
                }
            }
        }
    }
    
    
    private void addChildrenToNavbar(JsonNode node, VBox navbar, int depth)
    {
        if (depth == 1)
        {
            Label navbarLabel = new Label("Root Element");
            navbarLabel.setOnMouseClicked(event -> handleNavbarClick("Root Element", node));
            navbar.getChildren().add(navbarLabel);
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
                navbar.getChildren().add(label);
                addChildrenToNavbar(value, navbar, depth + 1);
            }
        }
    }
    
    private void handleNavbarClick(String name, JsonNode node)
    {
        controller.chooseNodeFromNavbar(name, node);
    }
    
}
