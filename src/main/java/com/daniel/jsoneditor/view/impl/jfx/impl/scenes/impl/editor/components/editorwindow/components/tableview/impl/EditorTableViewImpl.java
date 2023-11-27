package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
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
        setRowFactory(new Callback<>()
        {
            @Override
            public TableRow<JsonNodeWithPath> call(TableView<JsonNodeWithPath> param)
            {
                return new TableRow<>()
                {
                    @Override
                    protected void updateItem(JsonNodeWithPath item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (item == null)
                        {
                            setTooltip(null);
                        }
                        else
                        {
                            setTooltip(TooltipHelper.makeTooltipFromJsonNode(item.getNode()));
                        }
                    }
                };
            }
        });
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
            columns.add(createFollowReferenceButtonColumn());
            // we add a column of delete buttons
            columns.add(createDeleteButtonColumn());
        }
        setItems(elements);
        getColumns().clear();
        getColumns().addAll(columns);
    }
    
    private TableColumn<JsonNodeWithPath, String> createFollowReferenceButtonColumn()
    {
        TableColumn<JsonNodeWithPath, String> followReferenceColumn = new TableColumn<>();
        
        followReferenceColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPath()));
        
        followReferenceColumn.setCellFactory(param -> new TableCell<>()
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
                    String referencedPath = ReferenceHelper.resolveReference(nodeAtPath, model.getReferenceToObject(path), model);
                    if (referencedPath != null)
                    {
                        setGraphic(makeFollowReferenceButton(referencedPath));
                    }
                    else
                    {
                        setGraphic(null);
                    }
                }
            }
        });
        
        return followReferenceColumn;
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
            EditorTableColumn column = new EditorTableColumn(propertyName, isRequired);
            // every column holds one property of the array's items
            column.setCellValueFactory(data -> {
                JsonNodeWithPath jsonNodeWithPath = data.getValue();
                JsonNode valueNode = jsonNodeWithPath.getNode().get(propertyName);
                if (valueNode != null)
                {
                    String cellValue = valueNode.asText();
                    return new SimpleStringProperty(cellValue);
                }
                else
                {
                    return new SimpleStringProperty("");
                }
            });
            column.setCellFactory(column1 -> {
                if (propertyNode.isObject())
                {
                    List<String> types = SchemaHelper.getTypes(propertyNode);
                    if (types != null)
                    {
                        // TODO refactor to allow the user to choose what to display
                        if (types.contains("array") || types.contains("object"))
                        {
                            return makeOpenButtonTableCell(((EditorTableColumn) column1).getPropertyName());
                        }
                        else if (types.contains("string"))
                        {
                            
                            if (types.contains("integer") || types.contains("number"))
                            {
                                // display a TextTableCell that can also save itself as a number and not as a string if the user enters a
                                // number
                                return new TextTableCell(manager, model, true);
                            }
                            else
                            {
                                // a normal TextTableCell is enough. One that should save itself as string and not a number
                                return new TextTableCell(manager, model, false);
                            }
                            
                        }
                        else if (types.contains("integer") || types.contains("number"))
                        {
                            return makeNumberTableCell();
                        }
                    }
                }
                return new TextTableCell(manager, model, false);
            });
            columns.add(column);
        }
        return columns;
    }
    
    private TableCell<JsonNodeWithPath, String> makeOpenButtonTableCell(String pathToOpen)
    {
        return new TableCell<>()
        {
            private final Button button = new Button("Open");
            
            {
                button.setOnAction(event -> {
                    String item = getItem();
                    if (item != null)
                    {
                        JsonNodeWithPath jsonNodeWithPath = getTableRow().getItem();
                        // if the "open" button is clicked, we want to open that node in the current window
                        window.setSelectedPath(jsonNodeWithPath.getPath() + "/" + pathToOpen);
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty)
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
    
    private NumberTableCell makeNumberTableCell()
    {
        return new NumberTableCell(manager)
        {
            private final TextField textField = new TextField();
            
            {
                // Restrict input to numeric values only
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*"))
                    {
                        textField.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                });
                
                textField.setOnAction(event -> {
                    commitEdit(textField.getText());
                });
                
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (wasFocused && !isNowFocused)
                    {
                        commitEdit(textField.getText());
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                
                if (empty || item == null)
                {
                    setText(null);
                    setGraphic(null);
                }
                else
                {
                    setText(null);
                    textField.setText(item);
                    setGraphic(textField);
                }
            }
        };
    }
    
    private Button makeFollowReferenceButton(String path)
    {
        Button removeButton = new Button("Follow Reference");
        removeButton.setOnAction(event -> manager.selectInNewWindow(path));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
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
