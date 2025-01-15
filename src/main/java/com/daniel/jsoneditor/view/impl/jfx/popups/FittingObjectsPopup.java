package com.daniel.jsoneditor.view.impl.jfx.popups;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Consumer;


public class FittingObjectsPopup
{
    private final Consumer<ReferenceableObjectInstance> onItemSelected;
    
    private final Consumer<ReferenceableObjectInstance> onButtonClicked;
    
    
    private Window owner;
    
    private double posX;
    
    private double posY;
    
    private final Popup popup;
    
    private final ListView<ReferenceableObjectInstance> listView;
    
    public FittingObjectsPopup(ReadableModel model, Consumer<ReferenceableObjectInstance> onItemSelected, Consumer<ReferenceableObjectInstance> onButtonClicked)
    {
        this.onItemSelected = onItemSelected;
        this.onButtonClicked = onButtonClicked;
        listView = new ListView<>();
        listView.getStyleClass().add("popup-list-view");
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(listView);
        listView.setOnMouseClicked(event -> handleItemSelection(listView.getSelectionModel().getSelectedItem()));
        HBox.setHgrow(listView, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.NEVER);
        listView.setOnKeyPressed(event ->
        {
            // accept on enter
            if (event.getCode() == KeyCode.ENTER)
            {
                handleItemSelection(
                        listView.getSelectionModel().getSelectedItem());
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
                            Label label = new Label(item.getKey());
                            label.setTooltip(TooltipHelper.makeTooltipFromPath(model, item.getPath()));
                            
                            
                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);
                            
                            Button linkButton = new Button();
                            ButtonHelper.setButtonImage(linkButton, "/icons/material/darkmode/outline_link_white_24dp.png");
                            linkButton.setOnAction(event ->
                            {
                                handleItemSelection(item);
                            });
                            linkButton.setTooltip(new Tooltip("Link to this object"));
                            
                            Button duplicateButton = new Button();
                            ButtonHelper.setButtonImage(duplicateButton, "/icons/material/darkmode/outline_copy_white_24dp.png");
                            duplicateButton.setOnAction(event -> handleDuplicateCreation(item));
                            duplicateButton.setTooltip(new Tooltip("Create duplicate and link"));
                            hBox.getChildren().addAll(label, spacer, linkButton, duplicateButton);
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
        listView.heightProperty().addListener((observable, oldValue, newValue) -> moveVertically(newValue.doubleValue()));
    }
    
    public void setPopupPosition(Window owner, double x, double y)
    {
        this.owner = owner;
        this.posX = x;
        this.posY = y;
    }
    
    private void handleItemSelection(ReferenceableObjectInstance selectedItem)
    {
        if (selectedItem != null && onItemSelected != null)
        {
            onItemSelected.accept(selectedItem);
            hide();
        }
    }
    
    private void handleDuplicateCreation(ReferenceableObjectInstance selectedItem)
    {
        if (selectedItem != null && onButtonClicked != null)
        {
            onButtonClicked.accept(selectedItem);
            hide();
        }
    }
    
    public void setItems(List<ReferenceableObjectInstance> items)
    {
        listView.getItems().setAll(items);
        listView.setPrefWidth(maxListItemWidth(items));
        
        if (items.isEmpty() && popup.isShowing())
        {
            hide();
        }
        else if (!items.isEmpty())
        {
            show(owner, posX, posY);
        }
    }
    
    protected int maxListItemWidth(List<ReferenceableObjectInstance> items)
    {
        int maxWidth = 0;
        for (ReferenceableObjectInstance item : items)
        {
            int width = item.getKey().length() * 7 + 96; //96 = 2 buttons (hopefully)
            if (width > maxWidth)
            {
                maxWidth = width;
            }
        }
        return maxWidth;
    }
    
    public void moveVertically(double newHeight)
    {
        popup.setY(posY - newHeight);
    }
    
    public void show(Window owner, double x, double y)
    {
        if (!popup.isShowing() && owner != null)
        {
            popup.show(owner, x, y);
            moveVertically(popup.getHeight());
        }
    }
    
    public void hide()
    {
        if (popup.isShowing())
        {
            popup.hide();
        }
    }
}
