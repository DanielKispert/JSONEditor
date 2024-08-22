package com.daniel.jsoneditor.view.impl.jfx.popups;

import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import javafx.scene.control.ListView;
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
        HBox.setHgrow(listView, Priority.ALWAYS);
        popup = new Popup();
        popup.getContent().add(listView);
        listView.setOnMouseClicked(event ->
        {
            ReferenceableObjectInstance selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && onItemSelected != null)
            {
                onItemSelected.accept(selectedItem);
                hide();
            }
        });
    }
    
    public void setItems(List<ReferenceableObjectInstance> items)
    {
        if (items.isEmpty())
        {
            hide();
        }
        listView.getItems().setAll(items);
        adjustPopupSize();
    }
    
    public void show(Window owner, double x, double y)
    {
        if (!popup.isShowing())
        {
            popup.show(owner, x, y);
            adjustPopupSize();
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
        popup.setWidth(listView.getWidth());
        popup.setHeight(listView.getHeight());
    }
}
