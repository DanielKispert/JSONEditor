package com.daniel.jsoneditor.view.impl.jfx.buttons;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterColumnButton extends Button
{
    
    private final ContextMenu filterMenu;
    private final Supplier<List<String>> uniqueValuesSupplier;
    private final Runnable onFilterChanged;
    
    public FilterColumnButton(Supplier<List<String>> uniqueValuesSupplier, Runnable onFilterChanged)
    {
        super();
        this.uniqueValuesSupplier = uniqueValuesSupplier;
        this.onFilterChanged = onFilterChanged;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_filter_white_24dp.png");
        setOnAction(actionEvent -> showFilterMenu());
        filterMenu = new ContextMenu();
    }
    
    private void showFilterMenu()
    {
        filterMenu.getItems().clear();
        List<String> uniqueValues = uniqueValuesSupplier.get();
        for (String value : uniqueValues)
        {
            CheckMenuItem menuItem = new CheckMenuItem(escapeUnderscores(value));
            menuItem.setSelected(true);
            menuItem.setOnAction(e -> onFilterChanged.run());
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
    
    public List<String> getSelectedValues()
    {
        return filterMenu.getItems().stream()
                       .filter(item -> item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected())
                       .map(item -> revertUnderscores(item.getText()))
                       .collect(Collectors.toList());
    }
}
