package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.settings.ButtonSetting;
import jsoneditor.view.impl.jfx.impl.UIHandlerImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonEditorToolbar extends ToolBar
{
    private final Button addItemButton;
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    public JsonEditorToolbar(ReadableModel model, Controller controller)
    {
        this.controller = controller;
        this.model = model;
        Button removeSelectedObjectButton = new Button("Remove visible object");
        removeSelectedObjectButton.setOnAction(event ->
        {
            UIHandlerImpl.showConfirmDialog(controller::removeSelectedNode, "this will remove the json object currently visible in the editor window, not just the selected one!");
        });
        Button saveButton = new Button("Save to file");
        saveButton.setOnAction(event -> controller.saveToFile());
        addItemButton = new Button("Add Item");
        addItemButton.setOnAction(event -> controller.addNewNodeToSelectedArray());
        
        getItems().addAll(saveButton, removeSelectedObjectButton, addItemButton);
        getItems().addAll(makeSearchButtons());
        updateSelectedJson();
    }
    
    private List<Button> makeSearchButtons()
    {
        return Arrays.stream(model.getSettings().getButtons()).map(this::makeSearchButton).collect(Collectors.toList());
    }
    
    private Button makeSearchButton(ButtonSetting buttonSetting)
    {
        Button button = new Button(buttonSetting.getTitle());
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText("Enter Search Term:");
    
        button.setOnAction(actionEvent ->
        {
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> controller.searchForNode(buttonSetting.getTarget(), s));
        });
        return button;
        
        
    }
    
    public JsonNode getNodeFromPath(JsonNode node, String path)
    {
        String[] parts = path.split("/");
        
        for (int i = 0; i < parts.length; i++)
        {
            String part = parts[i];
            if (part.equals("?"))
            {
                JsonNode fields = node.get(parts[i - 1]);
                if (fields.isArray())
                {
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setHeaderText("Please enter a number:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent())
                    {
                        int index = Integer.parseInt(result.get());
                        node = fields.get(index);
                    }
                    else
                    {
                        return null;
                    }
                }
                else if (fields.isObject())
                {
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setHeaderText("Please enter some text:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent())
                    {
                        String key = result.get();
                        node = fields.get(key);
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else if (!part.isEmpty())
            {
                node = node.get(part);
            }
        }
        return node;
    }
    
    public void updateSelectedJson()
    {
        if (model.editingAnArray() && model.canAddMoreItems())
        {
            addItemButton.setVisible(true);
        }
        else
        {
            addItemButton.setVisible(false);
        }
    }
}
