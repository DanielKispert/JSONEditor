package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.util.List;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


/**
 * this dialog shows where a ReferenceableObject is used
 */
public class ShowUsagesDialog extends Dialog<String>
{
    
    private ListView<ReferenceToObjectInstance> listView;
    
    private HBox createUsagesBox(ReferenceToObjectInstance item)
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
    
    public ShowUsagesDialog(List<ReferenceToObjectInstance> items, JsonNodeWithPath jsonNodeWithPath)
    {
        this.setTitle("Usages of " + jsonNodeWithPath.getDisplayName());
        listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(items));
        listView.setCellFactory(param -> new ListCell<ReferenceToObjectInstance>()
        {
            @Override
            protected void updateItem(ReferenceToObjectInstance item, boolean empty)
            {
                super.updateItem(item, empty);
                setGraphic(item != null ? createUsagesBox(item) : null);
            }
        });
        
        listView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        
        setResultConverter(this::convertResult);
        
        listView.getSelectionModel().selectFirst();
        
        getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));
        getDialogPane().setContent(listView);
        getDialogPane().setPrefWidth(500);
        getDialogPane().setPrefHeight(400);
    }
    
    private void handleKeyPress(KeyEvent keyEvent)
    {
        if (keyEvent.getCode().equals(KeyCode.UP) && listView.getSelectionModel().getSelectedIndex() > 0)
        {
            listView.getSelectionModel().selectPrevious();
        }
        else if (keyEvent.getCode().equals(KeyCode.DOWN)
                && listView.getSelectionModel().getSelectedIndex() < listView.getItems().size() - 1)
        {
            listView.getSelectionModel().selectNext();
        }
        else if (keyEvent.getCode().equals(KeyCode.ENTER))
        {
            listView.fireEvent(
                    new ListView.EditEvent<>(listView, ListView.editCommitEvent(), listView.getSelectionModel().getSelectedItem(),
                            listView.getSelectionModel().getSelectedIndex()));
        }
    }
    
    private String convertResult(ButtonType buttonType)
    {
        if (buttonType.getButtonData().isCancelButton())
        {
            return null;
        }
        return listView.getSelectionModel().getSelectedItem().getPath();
    }
}
