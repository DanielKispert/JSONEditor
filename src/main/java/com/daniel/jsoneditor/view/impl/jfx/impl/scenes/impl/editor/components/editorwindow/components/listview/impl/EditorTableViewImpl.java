package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.EditorTableView;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.converter.DefaultStringConverter;


/**
 * shows a list of child objects of a selection. If the selection is an array, it shows a list of its items. If the selection is an
 * object, it shows the child nodes of the object.
 *
 */
public class EditorTableViewImpl extends EditorTableView
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private JsonNodeWithPath selection;
    
    public EditorTableViewImpl(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.manager = manager;
        this.model = model;
        this.controller = controller;
        VBox.setVgrow(this, Priority.ALWAYS);
        setEditable(true);
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        this.selection = nodeWithPath;
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath());
        ObservableList<JsonNodeWithPath> childNodes = FXCollections.observableArrayList(); //either a list of array items or object fields
        if (nodeWithPath.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                childNodes.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (nodeWithPath.isObject())
        {
            childNodes.add(nodeWithPath);
        }
        setView(childNodes, schema);
    }
    
    private void setView(ObservableList<JsonNodeWithPath> elements, JsonNode parentSchema)
    {
        JsonNode type = parentSchema.get("type");
        if (type == null)
        {
            return;
        }
        String typeText = type.asText();
        JsonNode childSchema = parentSchema;
        List<Pair<String, JsonNode>> properties = new ArrayList<>();
        List<TableColumn<JsonNodeWithPath, JsonNodeWithPath>> columns;
        if ("array".equals(typeText))
        {
            // Get the array node items and their properties from the schema
            childSchema = childSchema.get("items");
            Iterator<Entry<String, JsonNode>> iterator = childSchema.get("properties").fields();
        
            // Iterate over the properties (the bits the array items consist of). Every valid property will be one column
            while (iterator.hasNext())
            {
                Map.Entry<String, JsonNode> entry = iterator.next();
                // we only permit non-object and non-array properties in the list.
                String propertyType = NodeSearcher.getTypeFromNode(entry.getValue());
                if (propertyType != null && !"object".equals(propertyType) && !"array".equals(propertyType))
                {
                    properties.add(new Pair<>(entry.getKey(), entry.getValue()));
                }
            }
            // Create table columns dynamically based on the schema properties
            columns = createArrayTableColumns(properties);
        }
        else if ("object".equals(typeText))
        {
            columns = new ArrayList<>();
        
        } else {
            columns = new ArrayList<>();
        }
        
        
        setItems(elements);
        getColumns().clear();
        getColumns().addAll(columns);
    }
    
    private List<TableColumn<JsonNodeWithPath, JsonNodeWithPath>> createArrayTableColumns(List<Pair<String, JsonNode>> properties)
    {
        List<TableColumn<JsonNodeWithPath, JsonNodeWithPath>> tableColumns = createTableColumns(properties);
        // we add a column of delete buttons
        tableColumns.add(createDeleteButtonColumn());
        return tableColumns;
        
    }
    
    private TableColumn<JsonNodeWithPath, JsonNodeWithPath> createDeleteButtonColumn()
    {
        TableColumn<JsonNodeWithPath, JsonNodeWithPath> deleteColumn = new TableColumn<>("Delete");
        
        deleteColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        
        deleteColumn.setCellFactory(param -> new TableCell<>()
        {
            {
                setGraphic(makeRemoveButton(getItem().getPath()));
            }
    
            @Override
            protected void updateItem(JsonNodeWithPath item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                {
                    setGraphic(null);
                }
                else
                {
                    setGraphic(makeRemoveButton(item.getPath()));
                }
            }
        });
        
        return deleteColumn;
    }
    
    
    
    /**
     * creates columns for the table view
     */
    private List<TableColumn<JsonNodeWithPath, JsonNodeWithPath>> createTableColumns(List<Pair<String, JsonNode>> properties)
    {
        List<TableColumn<JsonNodeWithPath, JsonNodeWithPath>> columns = new ArrayList<>();
        for (Pair<String, JsonNode> property : properties)
        {
            String propertyName = property.getKey();
            JsonNode propertyNode = property.getValue();
            TableColumn<JsonNodeWithPath, JsonNodeWithPath> column = new TableColumn<>(propertyName);
            // every column holds one property of the array's items
            column.setCellValueFactory(data -> {
                JsonNodeWithPath jsonNodeWithPath = data.getValue();
                JsonNode valueNode = jsonNodeWithPath.getNode().get(propertyName);
                JsonNodeWithPath propertyNodeWithPath;
                if (valueNode != null)
                {
                    propertyNodeWithPath = new JsonNodeWithPath(valueNode, jsonNodeWithPath.getPath() + "/" + propertyName);
                }
                else
                {
                    propertyNodeWithPath = null;
                }
                return new SimpleObjectProperty<>(propertyNodeWithPath);
            });
            column.setCellFactory(column1 -> {
                if (propertyNode.isObject())
                {
                    JsonNode typeNode = propertyNode.get("type");
                    if (typeNode != null)
                    {
                        switch (typeNode.asText())
                        {
                            case "array":
                            case "object":
                                return makeButtonTableCell(column1.getText());
                            default:
                            case "integer":
                                return makeTextFieldTableCell();
                        }
                    }
                }
                return makeTextFieldTableCell();
            });
            columns.add(column);
        }
        return columns;
    }
    
    private TableCell<JsonNodeWithPath, JsonNodeWithPath> makeButtonTableCell(String pathToOpen)
    {
        return new TableCell<>()
        {
            private final Button button = new Button("Open");
    
            {
                button.setOnAction(event -> {
                    JsonNodeWithPath item = getTableRow().getItem();
                    manager.selectOnNavbar(item.getPath() + "/" + pathToOpen);
                });
            }
            
            @Override
            protected void updateItem(JsonNodeWithPath item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setGraphic(null);
                }
                else
                {
                    setGraphic(button);
                }
            }
        };
    }
    
    
    private TextFieldTableCell<JsonNodeWithPath, JsonNodeWithPath> makeTextFieldTableCell()
    {
        return new TextFieldTableCell<>();
    }
    
    
    public JsonNodeWithPath getSelection()
    {
        return selection;
    }
    
    public EditorWindowManager getManager()
    {
        return manager;
    }
    
    private Button makeRemoveButton(String path)
    {
        Button removeButton = new Button("X");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(event -> controller.removeNode(path));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
    
    
}
