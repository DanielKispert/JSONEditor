package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * Editor consists of a vbox holding a label and a listview
 *
 */
public class JsonEditorEditorWindow extends VBox
{
    private Label nameBar;
    
    private ListView<HBox> editor;
    
    private final Controller controller;
    
    public JsonEditorEditorWindow(ReadableModel model, Controller controller)
    {
        nameBar = new Label();
        editor = new ListView<>();
        VBox.setVgrow(editor, Priority.ALWAYS);
        this.controller = controller;
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
            // update editing window
            editor.getItems().clear();
            if (JsonNodeType.OBJECT.equals(selectedNode.getNodeType()))
            {
                // display all fields of non object or array type
                nameBar.setText(makeFancyName(nodeWithPath));
                editor.getItems().addAll(getObjectAsItems(nodeWithPath));
            }
            else if (JsonNodeType.ARRAY.equals(selectedNode.getNodeType()))
            {
                nameBar.setText(makeFancyName(nodeWithPath));
                // display all items in the array
                for (JsonNode arrayItem : selectedNode)
                {
                    
                    editor.getItems().add(getArrayItemAsRow(arrayItem));
                }
                if (model.canAddMoreItems())
                {
                    editor.getItems().add(makeAddButton());
                }
                
            }
        }
    }
    
    private String makeFancyName(JsonNodeWithPath node)
    {
        String path = node.getPath();
        String displayName = node.getDisplayName();
        String[] nameParts = path.split("/");
        nameParts[nameParts.length - 1] = displayName;
        StringBuilder newName = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++)
        {
            if (i != 0)
            {
                newName.append(" > ");
            }
            newName.append(nameParts[i]);
        }
        return newName.toString();
    }
    
    private List<HBox> getObjectAsItems(JsonNodeWithPath node)
    {
        List<HBox> hBoxes = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.getNode().fields(); it.hasNext(); )
        {
            Map.Entry<String, JsonNode> field = it.next();
            HBox hBox = new HBox();
            VBox keyField = makeFieldWithTitle("Key", field.getKey());
            hBox.getChildren().add(keyField);
            JsonNode value = field.getValue();
            if (value.isArray() || value.isObject())
            {
                hBox.getChildren().add(makeGoToButton(new JsonNodeWithPath(value, node.getPath() + "/" + field.getKey())));
            }
            else
            {
                hBox.getChildren().add(makeFieldWithTitle("Value", value.asText()));
            }
            hBoxes.add(hBox);
        }
        return hBoxes;
    }
    
    private HBox getArrayItemAsRow(JsonNode arrayItem)
    {
        HBox row = new HBox();
        Iterator<Map.Entry<String, JsonNode>> fields = arrayItem.fields();
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            if (value.getNodeType() != JsonNodeType.OBJECT && value.getNodeType() != JsonNodeType.ARRAY)
            {
                row.getChildren().add(makeFieldWithTitle(key, value.asText()));
            }
        }
        // add remove button
        row.getChildren().add(makeRemoveButton(arrayItem));
        return row;
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
    
    private Button makeRemoveButton(JsonNode object)
    {
        Button removeButton = new Button("X");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(event -> controller.removeNodeFromArray(object));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    
    private HBox makeAddButton()
    {
        Button addButton = new Button("+");
        HBox hBox = new HBox(addButton);
        addButton.prefWidthProperty().bind(hBox.widthProperty().divide(3));
        addButton.setOnAction(event -> controller.addNewNodeToSelectedArray());
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }
    
    private VBox makeFieldWithTitle(String title, String value)
    {
        VBox fieldBox = new VBox();
        Label fieldTitle = new Label(title);
        fieldTitle.setFont(new Font(12));
        TextField fieldInput = new TextField(value);
        fieldBox.getChildren().addAll(fieldTitle, fieldInput);
        HBox.setHgrow(fieldBox, Priority.ALWAYS);
        return fieldBox;
    }
}
