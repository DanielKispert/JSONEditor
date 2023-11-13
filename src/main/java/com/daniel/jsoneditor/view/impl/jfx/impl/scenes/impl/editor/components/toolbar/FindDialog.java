package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.List;
import java.util.stream.Collectors;


public class FindDialog extends Dialog<String>
{
    
    private final TextField searchField;
    
    private final ListView<Pair<String, String>> suggestionListView;
    
    private final List<Pair<String, String>> suggestions;
    
    private ButtonType searchButtonType;
    
    public FindDialog(List<Pair<String, String>> suggestions)
    {
        this.suggestions = suggestions;
        
        searchField = createSearchField();
        suggestionListView = createSuggestionListView();
        
        VBox layout = new VBox(suggestionListView, searchField);
        layout.setSpacing(5);
        
        getDialogPane().setContent(layout);
        ButtonType[] buttons = createDialogButtons();
        getDialogPane().getButtonTypes().addAll(buttons);
        
        setResultConverter(this::convertDialogResult);
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
    
    private String convertDialogResult(ButtonType dialogButton)
    {
        ButtonType searchButtonType = this.getDialogPane().getButtonTypes().get(0);
        return dialogButton == searchButtonType ? this.searchField.getText() : null;
    }
    
    private ListView<Pair<String, String>> createSuggestionListView()
    {
        ListView<Pair<String, String>> listView = new ListView<>();
        listView.setCellFactory(param -> new ListCell<>()
        {
            @Override
            protected void updateItem(Pair<String, String> item, boolean empty)
            {
                super.updateItem(item, empty);
                setGraphic(item != null ? createSuggestionBox(item) : null);
            }
        });
        return listView;
    }
    
    private HBox createSuggestionBox(Pair<String, String> suggestion)
    {
        HBox hbox = new HBox();
        Region spacer = new Region();
        
        Text suggestedText = new Text(suggestion.getKey());
        Text extraInfo = new Text(suggestion.getValue());
        
        extraInfo.setFill(Color.GRAY);
        extraInfo.setOpacity(0.5);
        
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        hbox.getChildren().addAll(suggestedText, spacer, extraInfo);
        hbox.setSpacing(8.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5));
        return hbox;
    }
    
    /**
     * Filters the suggestions ListView based on the current text entered by the user
     */
    private void filterSuggestionsBasedOn(String currentText)
    {
        List<Pair<String, String>> filteredSuggestions = suggestions.stream().filter(
                suggestion -> suggestion.getKey().toLowerCase().startsWith(currentText.toLowerCase())).collect(Collectors.toList());
        
        suggestionListView.setItems(FXCollections.observableArrayList(filteredSuggestions));
        
        if (!filteredSuggestions.isEmpty())
        {
            suggestionListView.getSelectionModel().select(0);
        }
    }
    
    /**
     * Handles KeyEvents - Autocompletes on TAB key press, and Submits the form on ENTER key press
     */
    private void handleKeyPress(KeyEvent keyEvent)
    {
        if (keyEvent.getCode().equals(KeyCode.TAB))
        {
            autoFill();
            keyEvent.consume();
        }
        else if (keyEvent.getCode().equals(KeyCode.ENTER))
        {
            submitSearch();
            keyEvent.consume();
        }
    }
    
    /**
     * Submits the search form programmatically
     */
    private void submitSearch()
    {
        Button searchButton = (Button) getDialogPane().lookupButton(searchButtonType);
        searchButton.fire();
    }
    
    /**
     * Auto-fills the search field with the longest common prefix from the suggestions
     */
    private void autoFill()
    {
        String currentText = searchField.getText().toLowerCase();
        List<String> matchingSuggestions = suggestions.stream().map(Pair::getKey).filter(
                suggestion -> suggestion.toLowerCase().startsWith(currentText)).collect(Collectors.toList());
        
        if (!matchingSuggestions.isEmpty())
        {
            String commonPrefix = getCommonPrefix(matchingSuggestions);
            searchField.setText(commonPrefix);
            suggestionListView.getSelectionModel().select(0);  // select first item in the list
            searchField.positionCaret(searchField.getText().length());
        }
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
    
    private ButtonType[] createDialogButtons()
    {
        searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        return new ButtonType[] { searchButtonType, cancelButtonType };
    }
    
}