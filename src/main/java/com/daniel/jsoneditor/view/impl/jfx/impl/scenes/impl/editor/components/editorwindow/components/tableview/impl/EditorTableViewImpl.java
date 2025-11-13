package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
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
    
    /**
     * Helper method to refresh parent selection - eliminates code duplication
     */
    private void refreshTable()
    {
        final JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null)
        {
            setSelection(parentNode);
        }
    }
    
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
        final List<EditorTableColumn> editorColumns = getColumns().stream()
                .filter(column -> column instanceof EditorTableColumn)
                .map(column -> (EditorTableColumn) column)
                .collect(Collectors.toList());
        
        filteredItems.setPredicate(item -> filterItem(item, editorColumns));
        
        final long shownItems = filteredItems.size();
        final long totalItems = getItems().size();
        logger.debug("Selected values for columns: " + editorColumns.stream()
                .map(EditorTableColumn::getSelectedValues)
                .collect(Collectors.toList()) + ". Showing " + shownItems + " of " + totalItems + " items.");
        
        editorColumns.forEach(EditorTableColumn::updatePrefWidth);
    }
    /**
     * @param item the item to filter
     * @param editorColumns pre-filtered list of EditorTableColumn instances
     * @return true if the item should be shown in the list, false if not
     */
    private boolean filterItem(JsonNodeWithPath item, List<EditorTableColumn> editorColumns)
    {
        for (final EditorTableColumn editorColumn : editorColumns)
        {
            final List<String> selectedValues = editorColumn.getSelectedValues();
            
            if (selectedValues == null)
            {
                return false;
            }
            
            if (selectedValues.isEmpty())
            {
                continue;
            }
            
            final JsonNode propertyNode = item.getNode().get(editorColumn.getPropertyName());
            final String cellValue = propertyNode == null ? "" : propertyNode.asText();
            if (!selectedValues.contains(cellValue))
            {
                return false;
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
        refreshTable();
    }
    
    public void handleItemRemoved(String path)
    {
        refreshTable();
    }
    
    public void handleItemChanged(String path)
    {
        // Check if the changed path affects any of our table items
        final String changedParentPath = PathHelper.getParentPath(path);
        boolean needsRefresh = false;
        
        for (JsonNodeWithPath item : allItems)
        {
            // If the changed path is a child of one of our table items
            if (item.getPath().equals(changedParentPath))
            {
                needsRefresh = true;
                break;
            }
            
            // If the changed path is one of our table items directly
            if (item.getPath().equals(path))
            {
                needsRefresh = true;
                break;
            }
        }
        
        // If any of our table items or their children changed, refresh the table
        if (needsRefresh)
        {
            refreshTable();
        }
    }
    
    public void handleItemMoved(ModelChange change)
    {
        if (change == null)
        {
            return;
        }

        final String path = change.getPath();
        if (!path.equals(parentPath))
        {
            return;
        }

        refreshTable();
    }
    
    public void handleSorted(String path)
    {
        refreshTable();
    }
    public void toggleTemporaryShowAllColumns()
    {
        temporaryShowAllColumns = !temporaryShowAllColumns;
        refreshColumnVisibility();
    }
    
    public boolean isTemporaryShowAllColumns()
    {
        return temporaryShowAllColumns;
    }
    
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
    
    private void showAllColumns()
    {
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            column.setVisible(true);
        }
    }
}
