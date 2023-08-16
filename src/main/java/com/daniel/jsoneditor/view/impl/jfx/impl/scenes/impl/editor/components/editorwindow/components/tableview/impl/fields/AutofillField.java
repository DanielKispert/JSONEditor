package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.List;
import java.util.stream.Collectors;

public class AutofillField extends ComboBox<String>
{
    
    
    private final List<String> allSuggestions;
    
    public AutofillField(TextTableCell parent, String text, List<String> allSuggestions, boolean valueMustBeASuggestion)
    {
        this.allSuggestions = allSuggestions;
        
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
    
        if (!valueMustBeASuggestion)
        {
            getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                                                           filterSuggestions(newValue));
        }
    }
    
    private void filterSuggestions(String filterText)
    {
        hide();
        List<String> filteredSuggestions = allSuggestions.stream()
                                                   .filter(suggestion -> suggestion.toLowerCase().contains(filterText.toLowerCase()))
                                                   .collect(Collectors.toList());
        getSelectionModel().clearSelection();
        getItems().setAll(filteredSuggestions);
        if (!filteredSuggestions.isEmpty())
        {
            if (isFocused() && !isShowing())
            {
                show();
            }
        }
        else
        {
            hide();
        }
    }
}
