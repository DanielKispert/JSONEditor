package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.scene.control.TextField;

public class EditorTextField extends TextField
{
    public EditorTextField(TextTableCell parent, String text)
    {
        super(text);
        setOnAction(event ->
        {
            parent.commitEdit(getText());
        });
        focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
        {
            if (wasFocused && !isNowFocused)
            {
                parent.commitEdit(getText());
            }
        });
    }
}
