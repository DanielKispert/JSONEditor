package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.Iterator;
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
                nameBar.setText(nodeWithPath.getName());
                editor.getItems().add(getObjectAsRow(selectedNode));
            }
            else if (JsonNodeType.ARRAY.equals(selectedNode.getNodeType()))
            {
                nameBar.setText(nodeWithPath.getName() + "(Array)");
                // display all items in the array
                for (JsonNode arrayItem : selectedNode)
                {
                    
                    editor.getItems().add(getObjectAsRow(arrayItem));
                }
                if (model.canAddMoreItems())
                {
                    editor.getItems().add(makeAddButton());
                }
                
            }
        }
    }
    
    private HBox getObjectAsRow(JsonNode object)
    {
        HBox row = new HBox();
        Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            if (value.getNodeType() != JsonNodeType.OBJECT && value.getNodeType() != JsonNodeType.ARRAY)
            {
                row.getChildren().add(makeFieldWithTitle(key, value.textValue()));
            }
        }
        // add remove button
        row.getChildren().add(makeRemoveButton(object));
        return row;
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
        HBox hBox = new HBox(new Button("+"));
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
