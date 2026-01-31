package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class WindowGitBlameToggleButton extends Button
{
    private final JsonEditorEditorWindow window;
    
    public WindowGitBlameToggleButton(JsonEditorEditorWindow window)
    {
        super();
        this.window = window;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_git_blame_white_24dp.png");
        updateState();
        setOnAction(event -> {
            window.toggleGitBlameForAllTables();
            updateState();
        });
    }
    
    private void updateState()
    {
        final boolean visible = window.isGitBlameVisible();
        setTooltip(new Tooltip(visible ? "Hide Git Blame" : "Show Git Blame"));
    }
}
