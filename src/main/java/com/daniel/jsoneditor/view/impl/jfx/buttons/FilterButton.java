package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.daniel.jsoneditor.view.impl.jfx.popups.FilterColumnPopup;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for filter buttons that use checkboxes to filter items
 */
public abstract class FilterButton extends Button
{
    private static final Logger logger = LoggerFactory.getLogger(FilterButton.class);
    
    protected final FilterColumnPopup filterPopup;
    protected final Runnable onFilterChanged;
    protected final Map<String, Boolean> valueStateMap;
    
    protected FilterButton(Runnable onFilterChanged)
    {
        super();
        this.onFilterChanged = onFilterChanged;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_filter_white_24dp.png");
        setOnAction(actionEvent -> showFilterPopup());
        this.valueStateMap = new HashMap<>();
        this.filterPopup = new FilterColumnPopup(valueStateMap, v -> {
            List<String> selectedItems = getSelectedItems();
            logger.debug("Filter button value changed - selectedItems: {}", selectedItems);
            onFilterChanged.run();
        });
    }
    
    private void showFilterPopup()
    {
        final List<String> items = getItemsForPopup();
        filterPopup.setItems(items);
        final Bounds bounds = localToScreen(getBoundsInLocal());
        filterPopup.setPopupPosition(getScene().getWindow(), bounds.getMinX(), bounds.getMinY());
        filterPopup.show();
    }
    
    protected abstract List<String> getItemsForPopup();
    
    protected abstract List<String> getAllAvailableItems();
    
    protected List<String> processSelectedItems(List<String> items)
    {
        return items;
    }
    
    /**
     * @return Selected items. Empty list means show all, null means show none
     */
    protected List<String> getSelectedItems()
    {
        final List<String> allItems = getAllAvailableItems();
        final long selectedCount = allItems.stream()
                                     .filter(item -> valueStateMap.getOrDefault(item, true))
                                     .count();
        
        if (selectedCount == allItems.size())
        {
            return List.of();
        }
        else if (selectedCount == 0)
        {
            return null;
        }
        else
        {
            final List<String> selectedItems = allItems.stream()
                           .filter(item -> valueStateMap.getOrDefault(item, true))
                           .collect(java.util.stream.Collectors.toList());
            return processSelectedItems(selectedItems);
        }
    }
    
    /**
     * Adds new items to the filter and sets them as selected by default
     * @param newItems items to add
     */
    public void addNewItemsAsSelected(Collection<String> newItems)
    {
        for (String item : newItems)
        {
            if (!valueStateMap.containsKey(item))
            {
                valueStateMap.put(item, true);
            }
        }
    }
}
