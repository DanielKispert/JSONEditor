package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;


public class EditorTextField extends TextField
{
    public EditorTextField(TextTableCell parent, String text)
    {
        super();
        // set preferred width to the width of the text entered plus a little extra
        textProperty().addListener((observable, oldValue, newValue) ->
        {
            Text text1 = new Text(newValue);
            double width = text1.getLayoutBounds().getWidth();
            setPrefWidth(width + 20);
        });
        setText(text); //intentionally done after adding the listener
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
        setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                parent.commitEdit(getText());
            }
        });
        setMaxWidth(Double.MAX_VALUE);

    }
}
