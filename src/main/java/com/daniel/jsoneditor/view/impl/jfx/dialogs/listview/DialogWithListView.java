package com.daniel.jsoneditor.view.impl.jfx.dialogs.listview;

import java.util.List;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.json.schema.reference.ReferencingInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ThemedDialog;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;


public abstract class DialogWithListView<T extends ReferencingInstance, R> extends ThemedDialog<R>
{
    protected ListView<T> listView;
    
    protected ButtonType okButtonType;
    
    protected ButtonType cancelButtonType;
    
    private CheckBox openInNewWindowCheckBox;
    
    public DialogWithListView(List<T> items)
    {
        initializeListView(items);
        okButtonType = new ButtonType(getOkButtonText(), ButtonBar.ButtonData.OK_DONE);
        cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        setResultConverter(this::convertResult);
    }
    
    /**
     * Adds a "Open in new window" CheckBox into the dialog's ButtonBar, left-aligned. The checkbox is prefilled from the given setting.
     * Call this from the subclass constructor after super() and after setting the dialog content.
     */
    protected void addOpenInNewWindowCheckBox(SettingsController settingsController)
    {
        openInNewWindowCheckBox = new CheckBox("Open in new window");
        openInNewWindowCheckBox.setSelected(settingsController.isOpenInNewWindow());
        
        // Insert the checkbox into the ButtonBar so it sits on the left, with buttons on the right
        ButtonBar.setButtonData(openInNewWindowCheckBox, ButtonBar.ButtonData.LEFT);
        getDialogPane().applyCss();
        getDialogPane().layout();
        javafx.scene.Node buttonBarNode = getDialogPane().lookup(".button-bar");
        if (buttonBarNode instanceof ButtonBar)
        {
            ((ButtonBar) buttonBarNode).getButtons().add(0, openInNewWindowCheckBox);
        }
    }
    
    /**
     * Returns true if the "Open in new window" checkbox is checked. Only valid after the dialog has been closed.
     */
    public boolean isOpenInNewWindowRequested()
    {
        return openInNewWindowCheckBox != null && openInNewWindowCheckBox.isSelected();
    }
    
    protected abstract String getOkButtonText();
    
    protected HBox createListItemBox(T item)
    {
        HBox hbox = new HBox();
        Region spacer = new Region();
        
        Text suggestedText = new Text(item.getKey());
        suggestedText.getStyleClass().add("dialog-list-cell-text");
        
        Text extraInfo = new Text(item.getFancyName());
        extraInfo.getStyleClass().add("dialog-list-cell-extra");
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        hbox.getChildren().addAll(suggestedText, spacer, extraInfo);
        hbox.setSpacing(8.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5));
        
        return hbox;
    }
    
    private void handleListViewKeyPress(KeyEvent keyEvent)
    {
        if (keyEvent.getCode().equals(KeyCode.ENTER))
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
    
    protected R convertResult(ButtonType buttonType)
    {
        if (buttonType.getButtonData().isCancelButton())
        {
            return null;
        }
        T selectedItem = listView.getSelectionModel().getSelectedItem();
        return selectedItem != null ? convertSelectedItem(selectedItem) : null;
    }
    
    /**
     * Converts the selected list item into the dialog result. Subclasses override this to produce their specific result type.
     */
    protected abstract R convertSelectedItem(T selectedItem);
    
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
        listView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleListViewKeyPress);
        listView.setPrefWidth(maxListItemWidth(items));
    }
    
    protected int maxListItemWidth(List<T> items)
    {
        int maxWidth = 0;
        for (T item : items)
        {
            int width = ((item.getKey() != null ? item.getKey().length() : 0) + item.getFancyName().length()) * 7;
            if (width > maxWidth)
            {
                maxWidth = width;
            }
        }
        return maxWidth;
    }
    
    protected abstract void onListItemDoubleClick(T item);
}
