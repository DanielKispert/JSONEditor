package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import com.daniel.jsoneditor.model.ReadableModel;
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
    
    private final ReadableModel model;
    
    public JsonEditorNamebar(EditorWindowManager manager, JsonEditorEditorWindow editorWindow, ReadableModel model)
    {
        super();
        this.manager = manager;
        this.editorWindow = editorWindow;
        this.model = model;
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
        StringBuilder fancyName = new StringBuilder();
        int startIndex = 0;
        int nextIndex;
        // first we grab the first path bit, then the first and second, and so on
        while ((nextIndex = path.indexOf("/", startIndex)) != -1)
        {
            String partialPath = path.substring(0, nextIndex);
            JsonNodeWithPath pathNode = model.getNodeForPath(partialPath);
            String displayName = pathNode.getDisplayName();
            fancyName.append(displayName);
            fancyName.append(" > ");
            startIndex = nextIndex + 1;
        }
    
        // the last part of the path has to be handled separately
        JsonNodeWithPath lastPathNode = model.getNodeForPath(path);
        fancyName.append(lastPathNode.getDisplayName());
    
        return fancyName.toString();
    }
}
