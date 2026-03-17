package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.List;
import java.util.function.Supplier;

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
    
    public List<String> getSelectedValues()
    {
        return getSelectedItems();
    }
}