package com.daniel.jsoneditor.view.impl.jfx.dialogs.listview;

import java.util.List;

import com.daniel.jsoneditor.model.json.schema.reference.HasKeyAndFancyName;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public abstract class DialogWithListView<T extends HasKeyAndFancyName> extends Dialog<String>
{
    protected ListView<T> listView;
    
    protected ButtonType okButtonType;
    
    protected ButtonType cancelButtonType;
    
    public DialogWithListView(List<T> items)
    {
        initializeListView(items);
        okButtonType = new ButtonType(getOkButtonText(), ButtonBar.ButtonData.OK_DONE);
        cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        setResultConverter(this::convertResult);
    }
    
    protected abstract String getOkButtonText();
    
    protected HBox createListItemBox(T item)
    {
        HBox hbox = new HBox();
        Region spacer = new Region();
        
        Text suggestedText = new Text(item.getKey());
        Text extraInfo = new Text(item.getFancyName());
        extraInfo.setFill(Color.GRAY);
        extraInfo.setOpacity(0.5);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        hbox.getChildren().addAll(suggestedText, spacer, extraInfo);
        hbox.setSpacing(8.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5));
        
        return hbox;
    }
    
    protected void handleKeyPress(KeyEvent keyEvent)
    {
        if (keyEvent.getCode() == KeyCode.DOWN)
        {
            navigateSuggestions(1);
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.UP)
        {
            navigateSuggestions(-1);
            keyEvent.consume();
        }
        else if (keyEvent.getCode().equals(KeyCode.ENTER))
        {
            handleDialogOk();
            keyEvent.consume();
        }
    }
    
    protected final void handleDialogOk()
    {
        Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
        okButton.fire();
    }
    protected String convertResult(ButtonType buttonType)
    {
        if (buttonType.getButtonData().isCancelButton())
        {
            return null;
        }
        return listView.getSelectionModel().getSelectedItem().getPath();
    }
    
    protected void initializeListView(List<T> items)
    {
        listView = new ListView<>();
        if (items != null)
        {
            listView.setItems(FXCollections.observableArrayList(items));
        }
        listView.setCellFactory(param -> {
            ListCell<T> cell = new ListCell<>()
            {
                @Override
                protected void updateItem(T item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setGraphic(item != null ? createListItemBox(item) : null);
                }
            };
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, click -> {
                if (click.getClickCount() == 2 && (!cell.isEmpty()))
                {
                    onListItemDoubleClick(cell.getItem());
                }
            });
            return cell;
        });
        listView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }
    
    protected abstract void onListItemDoubleClick(T item);
    
    /**
     * Navigates through the list of suggestions
     *
     * @param offset
     *         - indicates the number of positions to navigate. Positive for down and negative for up.
     */
    protected final void navigateSuggestions(int offset)
    {
        int currentIndex = listView.getSelectionModel().getSelectedIndex();
        int newIndex = currentIndex + offset;
        if (newIndex >= 0 && newIndex < listView.getItems().size())
        {
            listView.getSelectionModel().select(newIndex);
        }
    }
}
