package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.EditorTableColumn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * shows a list of child objects of a selection. If the selection is an array, every array item is one child node (= one row in the table).
 * If the selection is an
 * object, every child node of the object is one row in the table.
 */
public class EditorTableViewImpl extends EditorTableView
{
    
    private static final Logger logger = LoggerFactory.getLogger(EditorTableViewImpl.class);
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    //this list contains all items before the ui-filtering occurs
    private ObservableList<JsonNodeWithPath> allItems;
    
    private FilteredList<JsonNodeWithPath> filteredItems;
    
    /**
     * our table view shows one child item per row. We save the path of the parent item in case we want to paste something and there are no
     * child views to grab the parent path from
     */
    private String parentPath;
    
    // Extracted helper classes to reduce complexity
    private final TableSchemaProcessor schemaProcessor;
    
    private final TableColumnFactory columnFactory;
    
    // temporary override for hide empty columns setting
    private boolean temporaryShowAllColumns = false;
    
    public EditorTableViewImpl(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.model = model;
        this.controller = controller;
        this.schemaProcessor = new TableSchemaProcessor(model);
        this.columnFactory = new TableColumnFactory(manager, controller, model, window);
        VBox.setVgrow(this, Priority.ALWAYS);
        setEditable(true);
        setRowFactory(jsonNodeWithPathTableView -> new EditorTableRow(controller, this));
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }
    
    private void handleKeyPressed(KeyEvent event)
    {
        if (isFocused())
        {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(event))
            {
                copy();
            }
            else if (new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN).match(event))
            {
                paste();
            }
        }
    }
    
    private void copy()
    {
        JsonNodeWithPath selectedItem = getSelectionModel().getSelectedItem();
        controller.copyToClipboard(selectedItem != null ? selectedItem.getPath() : null);
    }
    
    private void paste()
    {
        JsonNodeWithPath selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null)
        {
            controller.pasteFromClipboardReplacingChild(selectedItem.getPath());
        }
        else
        {
            controller.pasteFromClipboardIntoParent(parentPath);
        }
    }
    
    @Override
    public void filter()
    {
        filteredItems.setPredicate(this::filterItem);
        
        long shownItems = filteredItems.stream().count();
        long totalItems = getItems().size();
        logger.debug("Selected values for columns: " + getColumns().stream().filter(column -> column instanceof EditorTableColumn)
                .map(column -> ((EditorTableColumn) column).getSelectedValues()).collect(Collectors.toList()) + ". Showing " + shownItems
                + " of " + totalItems + " items.");
        
        // trigger a resizing for all columns since their info could have changed now
        getColumns().forEach(column -> {
            if (column instanceof EditorTableColumn)
            {
                ((EditorTableColumn) column).updatePrefWidth();
            }
        });
        
    }
    
    /**
     * true if the item should be shown in the list, false if not
     */
    private boolean filterItem(JsonNodeWithPath item)
    {
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            if (column instanceof EditorTableColumn)
            {
                EditorTableColumn editorColumn = (EditorTableColumn) column;
                List<String> selectedValues = editorColumn.getSelectedValues();
                
                // If the list is null, show nothing
                if (selectedValues == null)
                {
                    return false;
                }
                
                // if the list is an empty list, then this column allows everything. Check the next column
                if (selectedValues.isEmpty())
                {
                    continue;
                }
                
                JsonNode propertyNode = item.getNode().get(editorColumn.getPropertyName());
                if (propertyNode == null)
                {
                    // If property doesn't exist, treat as empty string
                    return selectedValues.contains("");
                }
                String cellValue = propertyNode.asText();
                if (!selectedValues.contains(cellValue))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public String getSelectedPath()
    {
        return parentPath;
    }
    
    @Override
    protected double computePrefHeight(double v)
    {
        return (getItems().size() + 1) * 24;
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        parentPath = nodeWithPath.getPath();
        
        // Reset temporary override on new selection
        temporaryShowAllColumns = false;
        
        ReferenceToObject reference = model.getReferenceToObject(nodeWithPath.getPath());
        if (reference != null)
        {
            // TODO: Handle references
        }
        
        // Use the extracted schema processor to handle complex logic
        final TableSchemaProcessor.TableData tableData = schemaProcessor.processNode(nodeWithPath);
        setView(tableData);
    }
    
    @Override
    public void focusItem(String itemPath)
    {
        if (itemPath != null && !itemPath.isEmpty())
        {
            String[] pathSplit = itemPath.split("/");
            String name = pathSplit[pathSplit.length - 1];
            
            int index;
            try
            {
                index = Integer.parseInt(name);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Couldn't parse index from path: " + itemPath);
                return;
            }
            
            // Check if the index is within the bounds of TableView's items
            if (index >= 0 && index < getItems().size())
            {
                scrollTo(index);
                getSelectionModel().select(index);
            }
        }
    }
    
    @Override
    public ObservableList<JsonNodeWithPath> getUnfilteredItems()
    {
        return allItems;
    }
    
    @Override
    public List<String> getCurrentlyDisplayedPaths()
    {
        return getItems().stream().map(JsonNodeWithPath::getPath).collect(Collectors.toList());
    }
    
    private void setView(TableSchemaProcessor.TableData tableData)
    {
        this.allItems = tableData.getNodes();
        
        // Use the column factory to create columns
        final List<TableColumn<JsonNodeWithPath, String>> columns = columnFactory.createColumns(tableData.getProperties(),
                tableData.isArray(), this);
        
        filteredItems = new FilteredList<>(allItems);
        setItems(filteredItems);
        getColumns().clear();
        getColumns().addAll(columns);
        
        if (tableData.isArray() && controller.getSettingsController().hideEmptyColumns() && !temporaryShowAllColumns)
        {
            hideEmptyColumns();
        }
    }
    
    private void hideEmptyColumns()
    {
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            if (column instanceof EditorTableColumn)
            {
                final boolean required = ((EditorTableColumn) column).isRequired();
                if (!required)
                {
                    boolean empty = true;
                    
                    for (int i = 0; i < getItems().size(); i++)
                    {
                        if (column.getCellData(i) != null && !column.getCellData(i).toString().isEmpty())
                        {
                            empty = false;
                            break;
                        }
                    }
                    column.setVisible(!empty);
                }
            }
        }
    }
    
    // Granular update methods for specific model changes
    public void handleItemAdded(String path)
    {
        // Refresh the table to show the new item
        JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null)
        {
            setSelection(parentNode);
        }
    }
    
    public void handleItemRemoved(String path)
    {
        // Refresh the table to remove the deleted item
        JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null)
        {
            setSelection(parentNode);
        }
    }
    
    public void handleItemChanged(String path)
    {
        // update visibility button state
        
        String parentPath = PathHelper.getParentPath(path);
        // Find and update the specific item in the table
        for (int i = 0; i < allItems.size(); i++)
        {
            JsonNodeWithPath item = allItems.get(i);
            String listItemPath = item.getPath();
            if (listItemPath.equals(parentPath))
            {
                // the item that changed is a child of this item
                JsonNodeWithPath updatedChild = model.getNodeForPath(path);
                String childPath = PathHelper.getLastPathSegment(path);
                if (item.isArray())
                {
                    ArrayNode array = (ArrayNode) item.getNode();
                    int index = Integer.parseInt(childPath);
                    array.set(index, updatedChild.getNode());
                }
                else if (item.isObject())
                {
                    ((ObjectNode) item.getNode()).set(childPath, updatedChild.getNode());
                }
            }
            if (listItemPath.equals(path))
            {
                // the item itself changed
                JsonNodeWithPath updatedNode = model.getNodeForPath(path);
                if (updatedNode != null)
                {
                    allItems.set(i, updatedNode);
                }
                break;
            }
        }
    }
    
    public void handleItemMoved(String path)
    {
        // Refresh the entire table to show new order
        JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null)
        {
            setSelection(parentNode);
        }
    }
    
    public void handleSorted(String path)
    {
        // Refresh the entire table to show sorted order
        JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null)
        {
            setSelection(parentNode);
        }
    }
    
    /**
     * Toggles the temporary override for showing all columns
     */
    public void toggleTemporaryShowAllColumns()
    {
        temporaryShowAllColumns = !temporaryShowAllColumns;
        refreshColumnVisibility();
    }
    
    /**
     * Returns true if all columns are currently shown due to temporary override
     */
    public boolean isTemporaryShowAllColumns()
    {
        return temporaryShowAllColumns;
    }
    
    /**
     * Refreshes column visibility based on current settings and temporary override
     */
    private void refreshColumnVisibility()
    {
        if (temporaryShowAllColumns)
        {
            showAllColumns();
        }
        else if (controller.getSettingsController().hideEmptyColumns())
        {
            hideEmptyColumns();
        }
    }
    
    /**
     * Shows all columns regardless of empty state
     */
    private void showAllColumns()
    {
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            column.setVisible(true);
        }
    }
}
