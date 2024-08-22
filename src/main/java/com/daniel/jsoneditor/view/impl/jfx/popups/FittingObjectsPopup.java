package com.daniel.jsoneditor.view.impl.jfx.popups;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;
import java.util.function.Consumer;


public class FittingObjectsPopup
{
    private final Popup popup;
    
    private final ListView<ReferenceableObjectInstance> listView;
    
    public FittingObjectsPopup(Consumer<ReferenceableObjectInstance> onItemSelected)
    {
        listView = new ListView<>();
        listView.getStyleClass().add("popup-list-view");
        HBox.setHgrow(listView, Priority.ALWAYS);
        popup = new Popup();
        popup.getContent().add(listView);
        listView.setOnMouseClicked(event -> handleItemSelection(onItemSelected));
        listView.setOnKeyPressed(event -> {
            // accept on enter
            if (event.getCode() == KeyCode.ENTER)
            {
                handleItemSelection(onItemSelected);
            }
            // hide on escape
            else if (event.getCode() == KeyCode.ESCAPE)
            {
                hide();
            }
        });
    }
    
    private void handleItemSelection(Consumer<ReferenceableObjectInstance> onItemSelected)
    {
        ReferenceableObjectInstance selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && onItemSelected != null)
        {
            onItemSelected.accept(selectedItem);
            hide();
        }
    }
    
    public void setItems(List<ReferenceableObjectInstance> items)
    {
        listView.getItems().setAll(items);
        if (items.isEmpty() && popup.isShowing())
        {
            hide();
        }
        else if (!items.isEmpty() && !popup.isShowing())
        {
            show(popup.getOwnerWindow(), popup.getX(), popup.getY());
        }
    }
    
    public void show(Window owner, double x, double y)
    {
        if (!popup.isShowing() && owner != null)
        {
            popup.show(owner, x, y);
            //adjustPopupSize();
        }
    }
    
    public void hide()
    {
        if (popup.isShowing())
        {
            popup.hide();
        }
    }
    
    private void adjustPopupSize()
    {
        double itemHeight = 24; // Approximate height of each item
        double maxHeight = 200; // Maximum height of the popup
        double newHeight = Math.min(listView.getItems().size() * itemHeight, maxHeight);
        System.out.println("Setting Popup Dimensions to " + listView.getWidth() + "x" + newHeight);
        popup.setWidth(listView.getWidth());
        popup.setHeight(newHeight);
    }
    
    private void adjustPopupPosition()
    {
    
    }
}
