package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GraphFilterButton extends FilterButton
{
    private final Supplier<Collection<String>> edgeNamesSupplier;
    
    public GraphFilterButton(Supplier<Collection<String>> edgeNamesSupplier, Runnable onFilterChanged)
    {
        super(onFilterChanged);
        this.edgeNamesSupplier = edgeNamesSupplier;
    }
    
    @Override
    protected List<String> getItemsForPopup()
    {
        return getEdgeNamesList();
    }
    
    @Override
    protected List<String> getAllAvailableItems()
    {
        return getEdgeNamesList();
    }
    
    private List<String> getEdgeNamesList()
    {
        return new ArrayList<>(edgeNamesSupplier.get());
    }
    
    public List<String> getSelectedEdgeNames()
    {
        return getSelectedItems();
    }
}
