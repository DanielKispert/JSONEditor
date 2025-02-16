package com.daniel.jsoneditor.view.impl.jfx.buttons;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterColumnButton extends Button
{
    
    private final ContextMenu filterMenu;
    private final Supplier<List<String>> uniqueValuesSupplier;
    private final Runnable onFilterChanged;
    private final Map<String, Boolean> valueStateMap; //will be with escaped underscores
    
    public FilterColumnButton(Supplier<List<String>> uniqueValuesSupplier, Runnable onFilterChanged)
    {
        super();
        this.uniqueValuesSupplier = uniqueValuesSupplier;
        this.onFilterChanged = onFilterChanged;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_filter_white_24dp.png");
        setOnAction(actionEvent -> showFilterMenu());
        filterMenu = new ContextMenu();
        this.valueStateMap = new HashMap<>();
    }
    
    private void showFilterMenu()
    {
        filterMenu.getItems().clear();
        List<String> uniqueValues = uniqueValuesSupplier.get();
        
        // Add "Select All" checkbox
        CheckMenuItem selectAllItem = new CheckMenuItem("Select/Unselect All");
        selectAllItem.setSelected(valueStateMap.values().stream().allMatch(Boolean::booleanValue));
        selectAllItem.setOnAction(e ->
        {
            boolean selected = selectAllItem.isSelected();
            filterMenu.getItems().forEach(item ->
            {
                if (item instanceof CheckMenuItem)
                {
                    ((CheckMenuItem) item).setSelected(selected);
                    valueStateMap.put(revertUnderscores(item.getText()), selected);
                }
            });
            onFilterChanged.run();
        });
        filterMenu.getItems().add(selectAllItem);
        
        for (String value : uniqueValues)
        {
            CheckMenuItem menuItem = new CheckMenuItem(escapeUnderscores(value));
            menuItem.setSelected(valueStateMap.getOrDefault(value, true));
            menuItem.setOnAction(e ->
            {
                valueStateMap.put(revertUnderscores(menuItem.getText()), menuItem.isSelected());
                selectAllItem.setSelected(valueStateMap.values().stream().allMatch(Boolean::booleanValue));
                onFilterChanged.run();
            });
            filterMenu.getItems().add(menuItem);
        }
        filterMenu.show(this, Side.BOTTOM, 0, 0);
    }
    
    private String escapeUnderscores(String value)
    {
        return value.replaceAll("_", "__");
    }
    
    private String revertUnderscores(String value)
    {
        return value.replaceAll("__", "_");
    }
    
    /**
     * Convention: null: no value was selected, so show nothing, empty list: all values were selected, show everything
     */
    public List<String> getSelectedValues()
    {
        long selectedCount = valueStateMap.values().stream().filter(Boolean::booleanValue).count();
        if (selectedCount == 0)
        {
            return null;
        }
        else if (selectedCount == valueStateMap.size())
        {
            return List.of();
        }
        else
        {
            return valueStateMap.entrySet().stream()
                           .filter(Map.Entry::getValue)
                           .map(Map.Entry::getKey)
                           .map(this::revertUnderscores)
                           .collect(Collectors.toList());
        }
    }
}
