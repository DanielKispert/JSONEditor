package com.daniel.jsoneditor.view.impl.jfx.popups;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Consumer;


public class FittingObjectsPopup
{
    private Window owner;
    
    private double posX;
    
    private double posY;
    
    private final Popup popup;
    
    private final ListView<ReferenceableObjectInstance> listView;
    
    public FittingObjectsPopup(Consumer<ReferenceableObjectInstance> onItemSelected, Consumer<ReferenceableObjectInstance> onButtonClicked)
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
        listView.setCellFactory(new Callback<>()
        {
            @Override
            public ListCell<ReferenceableObjectInstance> call(ListView<ReferenceableObjectInstance> param)
            {
                return new ListCell<>()
                {
                    @Override
                    protected void updateItem(ReferenceableObjectInstance item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (item != null)
                        {
                            HBox hBox = new HBox();
                            hBox.setSpacing(10);
                            HBox.setHgrow(listView, Priority.ALWAYS);
                            
                            Button button = new Button();
                            ButtonHelper.setButtonImage(button, "/icons/material/darkmode/outline_copy_white_24dp.png");
                            button.setOnAction(event -> onButtonClicked.accept(item));
                            button.setTooltip(new Tooltip("Create duplicate and link"));
                            
                            hBox.getChildren().addAll(new Label(item.getKey()), button);
                            setGraphic(hBox);
                        }
                        else
                        {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }
    
    public void setPopupPosition(Window owner, double x, double y)
    {
        this.owner = owner;
        this.posX = x;
        this.posY = y;
        System.out.println("Setting Popup Position to " + x + ", " + y);
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
            show(owner, posX, posY);
        }
    }
    
    public void show(Window owner, double x, double y)
    {
        if (!popup.isShowing() && owner != null)
        {
            System.out.println("Showing Popup at " + x + ", " + y);
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
