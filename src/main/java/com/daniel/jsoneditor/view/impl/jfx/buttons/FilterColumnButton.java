package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterColumnButton extends FilterButton
{
    private final Supplier<List<String>> uniqueValuesSupplier;
    
    public FilterColumnButton(Supplier<List<String>> uniqueValuesSupplier, Runnable onFilterChanged)
    {
        super(onFilterChanged);
        this.uniqueValuesSupplier = uniqueValuesSupplier;
    }
    
    @Override
    protected List<String> getItemsForPopup()
    {
        return uniqueValuesSupplier.get();
    }
    
    @Override
    protected List<String> getAllAvailableItems()
    {
        return uniqueValuesSupplier.get();
    }
    
    @Override
    protected List<String> processSelectedItems(List<String> items)
    {
        return items.stream()
                   .map(this::revertUnderscores)
                   .collect(Collectors.toList());
    }
    
    public List<String> getSelectedValues()
    {
        return getSelectedItems();
    }
    
    private String revertUnderscores(String value)
    {
        return value.replaceAll("__", "_");
    }
}