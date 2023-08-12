package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.stream.Collectors;

public class AutofillField extends ComboBox<String>
{
    
    public AutofillField(TextTableCell parent, String text, List<String> allSuggestions, boolean valueMustBeASuggestion)
    {
        setEditable(!valueMustBeASuggestion);
        getItems().setAll(allSuggestions);
        setValue(text);
        
        setOnAction(event ->
        {
            parent.commitEdit(getEditor().getText());
        });
        
        focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
        {
            if (wasFocused && !isNowFocused)
            {
                parent.commitEdit(getEditor().getText());
            }
        });
        
        // Auto-complete behavior
        getEditor().textProperty().addListener((observable, oldValue, newValue) ->
        {
            getItems().setAll(allSuggestions.stream()
                                      .filter(suggestion -> suggestion.contains(newValue))
                                      .collect(Collectors.toList()));
            if (!isShowing())
            {
                show();
            }
        });
    }
}
