package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.view.impl.jfx.popups.FilterColumnPopup;
import javafx.scene.control.Button;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterColumnButton extends Button
{
    private final FilterColumnPopup filterPopup;
    private final Supplier<List<String>> uniqueValuesSupplier;
    private final Runnable onFilterChanged;
    private final Map<String, Boolean> valueStateMap;
    
    public FilterColumnButton(Supplier<List<String>> uniqueValuesSupplier, Runnable onFilterChanged)
    {
        super();
        this.uniqueValuesSupplier = uniqueValuesSupplier;
        this.onFilterChanged = onFilterChanged;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_filter_white_24dp.png");
        setOnAction(actionEvent -> showFilterPopup());
        this.valueStateMap = new HashMap<>();
        this.filterPopup = new FilterColumnPopup(valueStateMap, v -> onFilterChanged.run());
    }
    
    private void showFilterPopup()
    {
        List<String> uniqueValues = uniqueValuesSupplier.get();
        filterPopup.setItems(uniqueValues);
        Window window = getScene().getWindow();
        filterPopup.setPopupPosition(window, window.getX() + getLayoutX(), window.getY() + getLayoutY() + getHeight());
        filterPopup.show();
    }
    
    public List<String> getSelectedValues()
    {
        List<String> uniqueValues = uniqueValuesSupplier.get();
        long selectedCount = uniqueValues.stream()
                                     .filter(value -> valueStateMap.getOrDefault(value, true))
                                     .count();
        
        if (selectedCount == uniqueValues.size())
        {
            return List.of();
        }
        else if (selectedCount == 0)
        {
            return null;
        }
        else
        {
            return uniqueValues.stream()
                           .filter(value -> valueStateMap.getOrDefault(value, true))
                           .map(this::revertUnderscores)
                           .collect(Collectors.toList());
        }
    }
    
    private String revertUnderscores(String value)
    {
        return value.replaceAll("__", "_");
    }
}