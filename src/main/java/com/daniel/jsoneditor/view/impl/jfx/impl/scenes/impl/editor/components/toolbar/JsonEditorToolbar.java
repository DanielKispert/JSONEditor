package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.settings.ButtonSetting;
import com.daniel.jsoneditor.view.impl.jfx.impl.UIHandlerImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonEditorToolbar extends ToolBar
{
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    private final EditorWindowManager editorWindowManager;
    
    public JsonEditorToolbar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager)
    {
        this.controller = controller;
        this.model = model;
        this.editorWindowManager = editorWindowManager;
        Button removeSelectedObjectButton = new Button("Remove visible object");
        removeSelectedObjectButton.setOnAction(event ->
        {
            // TODO
            UIHandlerImpl.showConfirmDialog(() -> controller.removeNode(null), "this will remove the json object currently visible in the editor window, not just the selected one!");
        });
        removeSelectedObjectButton.setDisable(true);
        Button saveButton = new Button("Save to file");
        saveButton.setOnAction(event -> controller.saveToFile());
        
        getItems().addAll(saveButton, removeSelectedObjectButton);
        getItems().addAll(makeSearchButtons());
    }
    
    private List<Button> makeSearchButtons()
    {
        return Arrays.stream(model.getSettings().getButtons()).map(this::makeSearchButton).collect(Collectors.toList());
    }
    
    private Button makeSearchButton(ButtonSetting buttonSetting)
    {
        Button button = new Button(buttonSetting.getTitle());
    
        button.setOnAction(actionEvent ->
        {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setHeaderText("Enter Search Term:");
            Platform.runLater(() -> dialog.getEditor().requestFocus());
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s ->
            {
                String foundNode = model.searchForNode(buttonSetting.getTarget(), s);
                if (foundNode != null)
                {
                    if (editorWindowManager.canAnotherWindowBeAdded())
                    {
                        editorWindowManager.selectInNewWindow(foundNode);
                    }
                    else
                    {
                        editorWindowManager.selectFromNavbar(foundNode);
                    }
                }
            });
        });
        return button;
    }
}
