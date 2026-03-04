package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar;

import com.daniel.jsoneditor.view.impl.jfx.buttons.ToggleSidebarButton;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.FindDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.settings.ButtonSetting;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonEditorToolbar extends ToolBar
{
    
    private final ReadableModel model;
    
    private final EditorWindowManager editorWindowManager;
    
    private final JsonEditorNavbar navbar;
    
    public JsonEditorToolbar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager,
            JsonEditorNavbar navbar, ToggleSidebarButton toggleSidebarButton)
    {
        this.model = model;
        this.editorWindowManager = editorWindowManager;
        this.navbar = navbar;
        getItems().add(toggleSidebarButton);
        List<Button> searchButtons = makeSearchButtons();
        if (!searchButtons.isEmpty())
        {
            getItems().add(new Separator());
            getItems().addAll(searchButtons);
        }
    }
    
    private List<Button> makeSearchButtons()
    {
        return Arrays.stream(model.getSettings().getButtons()).map(this::makeSearchButton).collect(Collectors.toList());
    }
    
    private Button makeSearchButton(ButtonSetting buttonSetting)
    {
        Button button = new Button(buttonSetting.getTitle());
    
        button.setOnAction(actionEvent -> {
            FindDialog dialog = new FindDialog(model.getInstancesOfReferenceableObjectAtPath(buttonSetting.getTarget()));
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                navbar.selectPath(s);
                editorWindowManager.openPath(s);
            });
        });
        return button;
    }
}
