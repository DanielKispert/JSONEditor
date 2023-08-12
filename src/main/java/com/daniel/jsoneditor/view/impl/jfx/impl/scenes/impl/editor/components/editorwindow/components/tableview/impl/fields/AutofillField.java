package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.scene.control.ComboBox;

import java.util.List;

public class AutofillField extends ComboBox<String>
{
    
    public AutofillField(TextTableCell parent, String text, List<String> allSuggestions, boolean valueMustBeASuggestion)
    {
        setEditable(!valueMustBeASuggestion);
        setValue(text);
    
        setOnAction(event -> parent.commitEdit(getEditor().getText()));
    
        focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
        {
            if (wasFocused && !isNowFocused)
            {
                parent.commitEdit(getEditor().getText());
            }
        });
    
        getItems().setAll(allSuggestions);
    }
}
