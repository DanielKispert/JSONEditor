package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;


public class EditorTextField extends TextField
{
    public EditorTextField(final TextTableCell parent, final String text)
    {
        super();
        textProperty().addListener((observable, oldValue, newValue) -> {
            final Text newText = new Text(newValue);
            final double width = newText.getLayoutBounds().getWidth();
            setPrefWidth(width + 20);
            parent.onUserChangedText(newValue);
        });
        setText(text);
        setOnAction(event -> parent.commitEdit(getText()));
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused)
            {
                parent.commitEdit(getText());
            }
            parent.contentFocusChanged(isNowFocused);
        });
        setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER)
            {
                parent.commitEdit(getText());
            }
        });
        setMaxWidth(Double.MAX_VALUE);
    }
}
