package com.daniel.jsoneditor.view.impl.jfx.popups;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FilterColumnPopup extends BasePopup<List<String>>
{
    private final VBox vbox;
    private final Map<String, Boolean> valueStateMap;
    private final Consumer<Void> onFilterChanged;
    
    public FilterColumnPopup(Map<String, Boolean> valueStateMap, Consumer<Void> onFilterChanged)
    {
        super();
        this.valueStateMap = valueStateMap;
        this.onFilterChanged = onFilterChanged;
        vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);
        vbox.getStyleClass().add("popup-list-view");
        popup.getContent().add(vbox);
    }
    
    @Override
    public void setItems(List<String> uniqueValues)
    {
        vbox.getChildren().clear();
        
        // Add "Select All" checkbox
        CheckBox selectAllCheckBox = new CheckBox("Select/Unselect All");
        selectAllCheckBox.setSelected(valueStateMap.values().stream().allMatch(Boolean::booleanValue));
        selectAllCheckBox.setOnAction(e ->
        {
            boolean selected = selectAllCheckBox.isSelected();
            vbox.getChildren().forEach(node ->
            {
                if (node instanceof CheckBox)
                {
                    ((CheckBox) node).setSelected(selected);
                    valueStateMap.put(revertUnderscores(((CheckBox) node).getText()), selected);
                }
            });
            onFilterChanged.accept(null);
        });
        vbox.getChildren().add(selectAllCheckBox);
        
        for (String value : uniqueValues)
        {
            CheckBox checkBox = new CheckBox(escapeUnderscores(value));
            checkBox.setSelected(valueStateMap.getOrDefault(value, true));
            checkBox.setOnAction(e ->
            {
                valueStateMap.put(revertUnderscores(checkBox.getText()), checkBox.isSelected());
                selectAllCheckBox.setSelected(valueStateMap.values().stream().allMatch(Boolean::booleanValue));
                onFilterChanged.accept(null);
            });
            vbox.getChildren().add(checkBox);
        }
    }
    
    private String escapeUnderscores(String value)
    {
        return value.replaceAll("_", "__");
    }
    
    private String revertUnderscores(String value)
    {
        return value.replaceAll("__", "_");
    }
}
