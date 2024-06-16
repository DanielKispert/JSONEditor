package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ShowAsGraphButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ShowUsagesButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class JsonEditorNamebar extends HBox
{
    private final EditorWindowManager manager;
    
    private final JsonEditorEditorWindow editorWindow;
    
    private final Label nameLabel;
    
    private String parentPath;
    
    private String selectedPath;
    
    private final ReadableModel model;
    
    private final ShowUsagesButton showUsagesButton;
    
    private final ShowAsGraphButton showAsGraphButton;
    
    public JsonEditorNamebar(EditorWindowManager manager, JsonEditorEditorWindow editorWindow, ReadableModel model)
    {
        super();
        this.manager = manager;
        this.editorWindow = editorWindow;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        nameLabel = new Label();
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        showUsagesButton = new ShowUsagesButton(model, manager);
        showAsGraphButton = new ShowAsGraphButton(model, editorWindow);
        this.getChildren().addAll(makeSelectInNavbarButton(), makeGoToParentButton(), nameLabel, showAsGraphButton, showUsagesButton,
                makeCloseWindowButton());
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        this.selectedPath = selection.getPath();
        int index = selectedPath.lastIndexOf('/');
        if (index >= 0)
        {
            parentPath = selectedPath.substring(0, index);
        }
        else
        {
            parentPath = selectedPath;
        }
        nameLabel.setText(selection.makeNameIncludingPath(model));
        showUsagesButton.setSelection(selection);
        showAsGraphButton.setSelection(selection);
    }
    
    private Button makeSelectInNavbarButton()
    {
        Button selectInNavbarButton = new Button();
        ButtonHelper.setButtonImage(selectInNavbarButton, "/icons/material/darkmode/outline_adjust_white_24dp.png");
        selectInNavbarButton.setOnAction(actionEvent -> manager.selectOnNavbar(selectedPath));
        return selectInNavbarButton;
    }
    
    private Button makeGoToParentButton()
    {
        Button goToParentButton = new Button();
        ButtonHelper.setButtonImage(goToParentButton, "/icons/material/darkmode/outline_arrow_upward_white_24dp.png");
        goToParentButton.setOnAction(actionEvent -> editorWindow.setSelectedPath(parentPath));
        return goToParentButton;
    }
    
    private Button makeCloseWindowButton()
    {
        Button closeWindowButton = new Button();
        ButtonHelper.setButtonImage(closeWindowButton, "/icons/material/darkmode/outline_close_white_24dp.png");
        closeWindowButton.setOnAction(actionEvent -> manager.closeWindow(editorWindow));
        closeWindowButton.setAlignment(Pos.CENTER_RIGHT);
        return closeWindowButton;
    }
}
