package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar;

import java.util.List;
import java.util.stream.Collectors;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public class FindDialog extends Dialog<String>
{
    
    private final TextField searchField;
    
    private final ListView<ReferenceableObjectInstance> suggestionListView;
    
    private final List<ReferenceableObjectInstance> suggestions;
    
    private ButtonType searchButtonType;
    
    public FindDialog(List<ReferenceableObjectInstance> suggestions)
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
        if (dialogButton == searchButtonType)
        {
            return suggestionListView.getSelectionModel().getSelectedItem().getPath();
        }
        return null;
    }
    
    private ListView<ReferenceableObjectInstance> createSuggestionListView()
    {
        ListView<ReferenceableObjectInstance> listView = new ListView<>();
        listView.setCellFactory(param -> {
            ListCell<ReferenceableObjectInstance> cell = new ListCell<>()
            {
                @Override
                protected void updateItem(ReferenceableObjectInstance item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setGraphic(item != null ? createSuggestionBox(item) : null);
                }
            };
            // Adding mouse double click event
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, click -> {
                if (click.getClickCount() == 2 && (!cell.isEmpty()))
                {
                    searchField.setText(cell.getItem().getKey());
                    submitSearch();
                }
            });
            return cell;
        });
        return listView;
    }
    
    private HBox createSuggestionBox(ReferenceableObjectInstance suggestion)
    {
        HBox hbox = new HBox();
        Region spacer = new Region();
        
        Text suggestedText = new Text(suggestion.getKey());
        Text extraInfo = new Text(suggestion.getFancyName());
        
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
        List<ReferenceableObjectInstance> filteredSuggestions = suggestions.stream().filter(
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
            autofillText();
            keyEvent.consume();
        }
        else if (keyEvent.getCode().equals(KeyCode.ENTER))
        {
            submitSearch();
            keyEvent.consume();
        }
        else if (keyEvent.getCode().equals(KeyCode.UP))
        {
            navigateSuggestions(-1);
            keyEvent.consume();
        }
        else if (keyEvent.getCode().equals(KeyCode.DOWN))
        {
            navigateSuggestions(1);
            keyEvent.consume();
        }
    }
    
    /**
     * Navigates through the list of suggestions
     *
     * @param offset
     *         - indicates the number of positions to navigate. Positive for down and negative for up.
     */
    private void navigateSuggestions(int offset)
    {
        int currentIndex = suggestionListView.getSelectionModel().getSelectedIndex();
        int newIndex = currentIndex + offset;
        if (newIndex >= 0 && newIndex < suggestionListView.getItems().size())
        {
            suggestionListView.getSelectionModel().select(newIndex);
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