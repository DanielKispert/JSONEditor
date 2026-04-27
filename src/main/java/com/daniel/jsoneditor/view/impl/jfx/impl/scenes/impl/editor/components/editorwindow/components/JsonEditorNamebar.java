package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import java.util.List;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ShowUsagesButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.WindowGitBlameToggleButton;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.AreYouSureDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ReorderButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.VisibilityToggleButton;


public class JsonEditorNamebar extends HBox
{
    private final EditorWindowManager manager;
    
    private final JsonEditorEditorWindow editorWindow;
    
    private final Controller controller;
    
    private final Label nameLabel;
    
    private String parentPath;
    
    private String selectedPath;
    
    private final ReadableModel model;
    
    private final ShowUsagesButton showUsagesButton;
    
    private final ReorderButton reorderButton;
    
    private final VisibilityToggleButton visibilityToggleButton;
    
    public JsonEditorNamebar(EditorWindowManager manager, JsonEditorEditorWindow editorWindow, ReadableModel model, Controller controller,
            EditorTableView mainTableView)
    {
        super();
        this.manager = manager;
        this.editorWindow = editorWindow;
        this.controller = controller;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        nameLabel = new Label();
        nameLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        showUsagesButton = new ShowUsagesButton(model, manager, controller.getSettingsController());
        
        reorderButton = new ReorderButton(model, controller, () -> selectedPath);
        reorderButton.setVisible(false);
        reorderButton.setManaged(false);
        visibilityToggleButton = new VisibilityToggleButton(mainTableView);
        visibilityToggleButton.setVisible(false);
        visibilityToggleButton.setManaged(false);
        
        this.getChildren().addAll(makeSelectInNavbarButton(), makeGoToParentButton(), nameLabel, reorderButton,
                visibilityToggleButton, showUsagesButton);
        
        if (model.isGitBlameAvailable())
        {
            this.getChildren().add(new WindowGitBlameToggleButton(editorWindow));
        }
        
        this.getChildren().addAll(makeDeleteItemButton(), makeCloseWindowButton());
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
        final boolean isArray = selection.isArray();
        reorderButton.setVisible(isArray);
        reorderButton.setManaged(isArray);
        final boolean showVisibilityToggle = isArray && controller.getSettingsController().hideEmptyColumns();
        visibilityToggleButton.setVisible(showVisibilityToggle);
        visibilityToggleButton.setManaged(showVisibilityToggle);
    }

    /**
     * Refreshes visibility of array-specific buttons when settings change.
     */
    public void refreshButtonVisibility()
    {
        if (selectedPath != null)
        {
            final JsonNodeWithPath currentNode = model.getNodeForPath(selectedPath);
            if (currentNode != null)
            {
                setSelection(currentNode);
            }
        }
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
    
    private Button makeDeleteItemButton()
    {
        Button deleteItemButton = new Button();
        ButtonHelper.setButtonImage(deleteItemButton, "/icons/material/darkmode/outline_delete_white_24dp.png");
        deleteItemButton.setOnAction(actionEvent -> {
            AreYouSureDialog dialog = new AreYouSureDialog("Delete Node", "Are you sure you want to delete this node?",
                    "This will delete the node and all its children.");
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES)
                {
                    controller.removeNodes(List.of(selectedPath));
                }
            });
        });
        return deleteItemButton;
    }
}
