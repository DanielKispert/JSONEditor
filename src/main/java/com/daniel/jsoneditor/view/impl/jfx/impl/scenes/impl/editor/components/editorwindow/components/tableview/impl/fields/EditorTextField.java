package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;


public class EditorTextField extends TextField
{
    private final TextTableCell parent;

    public EditorTextField(final TextTableCell parent, final String text)
    {
        super();
        this.parent = parent;
        textProperty().addListener((observable, oldValue, newValue) -> {
            final Text newText = new Text(newValue);
            final double width = newText.getLayoutBounds().getWidth();
            setPrefWidth(width + 20);
            parent.onUserChangedText(newValue);
        });
        setText(text);
        setOnAction(event -> commitEdit());
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused)
            {
                commitEdit();
            }
            parent.contentFocusChanged(isNowFocused);
        });
        setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER)
            {
                commitEdit();
            }
        });
        setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Commits the current text to the parent cell. Called on Enter, focus loss, and action events.
     */
    public void commitEdit()
    {
        parent.commitEditFromCurrentControl(getText(), this);
    }
}
