package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class AutofillField extends ComboBox<String>
{
    
    
    private final List<String> allSuggestions;
    
    public AutofillField(TextTableCell parent, String text, List<String> allSuggestions, boolean valueMustBeASuggestion)
    {
        this.allSuggestions = allSuggestions;
        getEditor().textProperty().addListener((observable, oldValue, newValue) ->
        {
            Text text1 = new Text(newValue);
            double width = text1.getLayoutBounds().getWidth();
            setPrefWidth(width + 50);
            parent.onUserChangedText(newValue);
        });
        setEditable(!valueMustBeASuggestion);
        setValue(text);
        setMaxWidth(Double.MAX_VALUE);
        
        setOnAction(event -> parent.commitEdit(getEditor().getText()));
        
        focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
        {
            if (wasFocused && !isNowFocused)
            {
                parent.commitEdit(getEditor().getText());
            }
            parent.contentFocusChanged(isNowFocused);
        });
        getEditor().setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER)
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
