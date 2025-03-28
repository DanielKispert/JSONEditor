package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.FollowOrCreateButtonCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.EditorTableColumn;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.FollowRefOrOpenColumn;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


/**
 * shows a list of child objects of a selection. If the selection is an array, every array item is one child node (= one row in the table).
 * If the selection is an
 * object, every child node of the object is one row in the table.
 */
public class EditorTableViewImpl extends EditorTableView
{
    
    private static final Logger logger = LoggerFactory.getLogger(EditorTableViewImpl.class);
    
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final JsonEditorEditorWindow window;
    
    //this list contains all items before the ui-filtering occurs
    private ObservableList<JsonNodeWithPath> allItems;
    
    private FilteredList<JsonNodeWithPath> filteredItems;
    
    /**
     * our table view shows one child item per row. We save the path of the parent item in case we want to paste something and there are no
     * child views to grab the parent path from
     */
    private String parentPath;
    
    public EditorTableViewImpl(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.window = window;
        this.manager = manager;
        this.model = model;
        this.controller = controller;
        VBox.setVgrow(this, Priority.ALWAYS);
        setEditable(true);
        setRowFactory(jsonNodeWithPathTableView -> new EditorTableRow(model, controller, this));
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
        logger.debug("Selected values for columns: " + getColumns().stream()
                                                               .filter(column -> column instanceof EditorTableColumn)
                                                               .map(column -> ((EditorTableColumn) column).getSelectedValues())
                                                               .collect(Collectors.toList()) +
                             ". Showing " + shownItems + " of " + totalItems + " items.");
        
        // trigger a resizing for all columns since their info could have changed now
        getColumns().forEach(column ->
        {
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

                
                String cellValue = item.getNode().get(editorColumn.getPropertyName()).asText();
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
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath()).getSchemaNode();
        
        ReferenceToObject reference = model.getReferenceToObject(nodeWithPath.getPath());
        if (reference != null)
        {
            
            
        }
        
        
        ObservableList<JsonNodeWithPath> nodesToDisplay = FXCollections.observableArrayList(); //either a list of array items or object fields
        if (nodeWithPath.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                nodesToDisplay.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (nodeWithPath.isObject())
        {
            nodesToDisplay.add(nodeWithPath);
        }
        setView(nodesToDisplay, schema);
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
    
    private void setView(ObservableList<JsonNodeWithPath> elements, JsonNode parentSchema)
    {
        this.allItems = elements;
        JsonNode type = parentSchema.get("type");
        if (type == null)
        {
            return;
        }
        String typeText = type.asText();
        boolean isArray = "array".equals(typeText);
        JsonNode childSchema = parentSchema;
        List<Pair<Pair<String, Boolean>, JsonNode>> properties = new ArrayList<>();
        if (isArray)
        {
            // Get the array node items and their properties from the schema
            childSchema = childSchema.get("items");
        }
        List<String> requiredProperties = SchemaHelper.getRequiredProperties(childSchema);
        
        Iterator<Entry<String, JsonNode>> iterator = childSchema.get("properties").fields();
        
        while (iterator.hasNext())
        {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String propertyName = entry.getKey();
            boolean isRequired = requiredProperties.contains(propertyName);
            if (isArray)
            {
                // we only permit non-object and non-array properties in the list.
                String propertyType = NodeSearcher.getTypeFromNode(entry.getValue());
                if (propertyType != null && !"object".equals(propertyType) && !"array".equals(propertyType))
                {
                    properties.add(new Pair<>(new Pair<>(propertyName, isRequired), entry.getValue()));
                }
            }
            else
            {
                properties.add(new Pair<>(new Pair<>(propertyName, isRequired), entry.getValue()));
            }
        }
        
        List<TableColumn<JsonNodeWithPath, String>> columns = new ArrayList<>(createTableColumns(properties));
        if (isArray)
        {
            // we add a column that either holds the "follow reference" or "open array element" button
            columns.add(new FollowRefOrOpenColumn(model, manager));
            // we add a column of delete buttons.css
            columns.add(createDeleteButtonColumn());
        }
        filteredItems = new FilteredList<>(allItems);
        setItems(filteredItems);
        getColumns().clear();
        getColumns().addAll(columns);
        if (isArray && controller.getSettingsController().hideEmptyColumns())
        {
            
            for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
            {
                if (column instanceof EditorTableColumn)
                {
                    boolean required = ((EditorTableColumn) column).isRequired();
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
    }
    
    private TableColumn<JsonNodeWithPath, String> createDeleteButtonColumn()
    {
        TableColumn<JsonNodeWithPath, String> deleteColumn = new TableColumn<>("Delete");
        
        deleteColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPath()));
        
        deleteColumn.setCellFactory(param -> new TableCell<>()
        {
            @Override
            protected void updateItem(String path, boolean empty)
            {
                super.updateItem(path, empty);
                if (empty || path == null)
                {
                    setGraphic(null);
                }
                else
                {
                    setGraphic(makeRemoveButton(path));
                }
            }
        });
        
        return deleteColumn;
    }
    
    /**
     * creates columns for the table view
     */
    private List<EditorTableColumn> createTableColumns(List<Pair<Pair<String, Boolean>, JsonNode>> properties)
    {
        List<EditorTableColumn> columns = new ArrayList<>();
        for (Pair<Pair<String, Boolean>, JsonNode> property : properties)
        {
            String propertyName = property.getKey().getKey();
            boolean isRequired = property.getKey().getValue();
            JsonNode propertyNode = property.getValue();
            columns.add(new EditorTableColumn(manager, controller, model, window, this, propertyNode, propertyName, isRequired));
        }
        return columns;
    }
    
    private Button makeRemoveButton(String path)
    {
        Button removeButton = new Button();
        ButtonHelper.setButtonImage(removeButton, "/icons/material/darkmode/outline_delete_white_24dp.png");
        removeButton.setOnAction(event -> controller.removeNode(path));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    
}
