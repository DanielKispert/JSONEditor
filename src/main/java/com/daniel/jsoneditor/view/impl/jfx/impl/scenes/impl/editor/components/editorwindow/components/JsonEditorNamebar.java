package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;


public class JsonEditorNamebar extends HBox
{
    private final EditorWindowManager manager;
    
    private final JsonEditorEditorWindow editorWindow;
    
    private final Label nameLabel;
    
    private String parentPath;
    
    private String selectedPath;
    
    public JsonEditorNamebar(EditorWindowManager manager, JsonEditorEditorWindow editorWindow)
    {
        super();
        this.manager = manager;
        this.editorWindow = editorWindow;
        HBox.setHgrow(this, Priority.ALWAYS);
        nameLabel = new Label();
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        this.getChildren().addAll(makeSelectInNavbarButton(), makeGoToParentButton(), nameLabel, makeCloseWindowButton());
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
        nameLabel.setText(makeFancyName(selection));
    }
    
    private Button makeSelectInNavbarButton()
    {
        Button selectInNavbarButton = new Button();
        selectInNavbarButton.setText("<-");
        selectInNavbarButton.setOnAction(actionEvent -> manager.selectOnNavbar(selectedPath));
        return selectInNavbarButton;
    }
    
    private Button makeGoToParentButton()
    {
        Button goToParentButton = new Button();
        goToParentButton.setText("^");

        goToParentButton.setOnAction(actionEvent -> editorWindow.setSelectedPath(parentPath));
        return goToParentButton;
    }
    
    private Button makeCloseWindowButton()
    {
        Button closeWindowButton = new Button();
        closeWindowButton.setText("X");
        closeWindowButton.setTextFill(Color.RED);
        closeWindowButton.setOnAction(actionEvent -> manager.closeWindow(editorWindow));
        closeWindowButton.setAlignment(Pos.CENTER_RIGHT);
        return closeWindowButton;
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
}
