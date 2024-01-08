package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.util.List;
import java.util.stream.Collectors;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.listview.DialogWithListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;


public class FindDialog extends DialogWithListView<ReferenceableObjectInstance>
{
    
    private final TextField searchField;
    
    private final List<ReferenceableObjectInstance> suggestions;
    
    public FindDialog(List<ReferenceableObjectInstance> suggestions)
    {
        super(suggestions);
        this.suggestions = suggestions;
        
        searchField = createSearchField();
        
        VBox layout = new VBox(listView, searchField);
        layout.setSpacing(5);
        
        getDialogPane().setContent(layout);
        
        Platform.runLater(searchField::requestFocus);
    }
    
    private TextField createSearchField()
    {
        TextField textField = new TextField();
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setPromptText("Search...");
        textField.setPrefWidth(800);
        // Filtering the suggestions as the user types
        textField.textProperty().addListener((observableValue, oldValue, newValue) -> filterSuggestionsBasedOn(newValue));
        textField.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        return textField;
    }
    
    /**
     * Filters the suggestions ListView based on the current text entered by the user
     */
    private void filterSuggestionsBasedOn(String currentText)
    {
        List<ReferenceableObjectInstance> filteredSuggestions = suggestions.stream().filter(
                suggestion -> suggestion.getKey().toLowerCase().contains(currentText.toLowerCase())).collect(Collectors.toList());
        
        listView.setItems(FXCollections.observableArrayList(filteredSuggestions));
        
        if (!filteredSuggestions.isEmpty())
        {
            listView.getSelectionModel().select(0);
        }
    }
    
    /**
     * Handles KeyEvents - Autocompletes on TAB key press, and Submits the form on ENTER key press
     */
    @Override
    protected void handleKeyPress(KeyEvent keyEvent)
    {
        super.handleKeyPress(keyEvent);
        if (!keyEvent.isConsumed())
        {
            if (keyEvent.getCode().equals(KeyCode.TAB))
            {
                autofillText();
                keyEvent.consume();
            }
        }
    }
    
    @Override
    protected void onListItemDoubleClick(ReferenceableObjectInstance item)
    {
        searchField.setText(item.getKey());
        handleDialogOk();
    }
    
    /**
     * Autofills the search field with the longest common prefix from the suggestions
     */
    private void autofillText()
    {
        String currentText = searchField.getText().toLowerCase();
        List<String> matchingSuggestions = suggestions.stream().map(ReferenceableObjectInstance::getKey).filter(
                suggestion -> suggestion.toLowerCase().startsWith(currentText)).collect(Collectors.toList());
        
        if (!matchingSuggestions.isEmpty())
        {
            String commonPrefix = getCommonPrefix(matchingSuggestions);
            searchField.setText(commonPrefix);
            listView.getSelectionModel().select(0);  // select first item in the list
            searchField.positionCaret(searchField.getText().length());
        }
    }
    
    @Override
    protected String getOkButtonText()
    {
        return "Search";
    }
    
    /**
     * Returns the longest common prefix in a list of Strings
     */
    private String getCommonPrefix(List<String> prefixes)
    {
        StringBuilder commonPrefix = new StringBuilder(prefixes.get(0));
        for (String prefix : prefixes)
        {
            while (!prefix.startsWith(commonPrefix.toString()))
            {
                commonPrefix.setLength(commonPrefix.length() - 1);
            }
        }
        return commonPrefix.toString();
    }
    
}