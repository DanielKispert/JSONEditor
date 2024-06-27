package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * shows a list of child objects of a selection. If the selection is an array, every array item is one child node (= one row in the table).
 * If the selection is an
 * object, every child node of the object is one row in the table.
 */
public class EditorTableViewImpl extends EditorTableView
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final JsonEditorEditorWindow window;
    
    public EditorTableViewImpl(EditorWindowManager manager, JsonEditorEditorWindow window, ReadableModel model, Controller controller)
    {
        this.window = window;
        this.manager = manager;
        this.model = model;
        this.controller = controller;
        VBox.setVgrow(this, Priority.ALWAYS);
        setEditable(true);
    }
    
    private void setColumnWidths()
    {
        double[] maxCellWidths = new double[getColumns().size()];
        
        int columnIndex = 0;
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            // Get the column title width
            String columnTitle = column.getText();
            maxCellWidths[columnIndex] = columnTitle.length() * 6 + 10;  // Approximate width calculation
            
            for (JsonNodeWithPath item : getItems())
            {
                Object cellValue = column.getCellData(item);
                if (cellValue != null)
                {
                    // Calculate the cell content width
                    maxCellWidths[columnIndex] = Math.max(maxCellWidths[columnIndex],
                            cellValue.toString().length() * 6 + 10);  // Approximate width calculation
                }
            }
            columnIndex++;
        }
        
        columnIndex = 0;
        for (TableColumn<JsonNodeWithPath, ?> column : getColumns())
        {
            column.setPrefWidth(maxCellWidths[columnIndex++]);
        }
    }
    
    @Override
    protected double computePrefHeight(double v)
    {
        return (getItems().size() + 1) * 24;
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath()).getSchemaNode();
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
    
    private void setView(ObservableList<JsonNodeWithPath> elements, JsonNode parentSchema)
    {
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
            columns.add(createFollowReferenceOrOpenElementButtonColumn());
            // we add a column of delete buttons.css
            columns.add(createDeleteButtonColumn());
        }
        
        setItems(elements);
        getColumns().clear();
        getColumns().addAll(columns);
        setColumnWidths();
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
    
    private TableColumn<JsonNodeWithPath, String> createFollowReferenceOrOpenElementButtonColumn()
    {
        TableColumn<JsonNodeWithPath, String> followReferenceOrOpenColumn = new TableColumn<>();
        
        followReferenceOrOpenColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPath()));
        
        followReferenceOrOpenColumn.setCellFactory(param -> new TableCell<>()
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
                    JsonNodeWithPath nodeAtPath = model.getNodeForPath(path);
                    String referencedPath = ReferenceHelper.resolveReference(nodeAtPath, model);
                    if (referencedPath != null)
                    {
                        setGraphic(makeFollowReferenceButton(referencedPath));
                    }
                    else
                    {
                        setGraphic(makeOpenArrayElementButton(path));
                    }
                }
            }
        });
        
        return followReferenceOrOpenColumn;
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
            columns.add(new EditorTableColumn(manager, controller, model, window, propertyNode, propertyName, isRequired));
        }
        return columns;
    }
    
    private Button makeFollowReferenceButton(String path)
    {
        Button removeButton = new Button("Follow Reference");
        removeButton.setOnAction(event -> manager.selectInNewWindow(path));
        removeButton.setTooltip(TooltipHelper.makeTooltipFromJsonNode(model.getNodeForPath(path).getNode()));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    private Button makeOpenArrayElementButton(String path)
    {
        Button openArrayElementButton = new Button();
        ButtonHelper.setButtonImage(openArrayElementButton, "/icons/material/darkmode/outline_open_in_new_white_24dp.png");
        openArrayElementButton.setOnAction(event -> manager.selectInNewWindow(path));
        openArrayElementButton.setTooltip(TooltipHelper.makeTooltipFromJsonNode(model.getNodeForPath(path).getNode()));
        openArrayElementButton.setMaxHeight(Double.MAX_VALUE);
        return openArrayElementButton;
    }
    
    private Button makeRemoveButton(String path)
    {
        Button removeButton = new Button();
        ButtonHelper.setButtonImage(removeButton, "/icons/material/darkmode/outline_close_white_24dp.png");
        removeButton.setOnAction(event -> controller.removeNode(path));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
}
