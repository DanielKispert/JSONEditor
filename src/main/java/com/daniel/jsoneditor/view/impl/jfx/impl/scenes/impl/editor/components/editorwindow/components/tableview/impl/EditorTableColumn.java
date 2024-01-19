package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import java.util.List;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;


public class EditorTableColumn extends TableColumn<JsonNodeWithPath, String>
{
    
    private final String propertyName;
    private final boolean isRequired;
    
    private final EditorWindowManager manager;
    
    private final ReadableModel model;
    
    private final JsonEditorEditorWindow window;
    
    /**
     * true if this column holds the key property of a referenceable object, false otherwise
     */
    private final boolean holdsKeyOfReferenceableObject;
    
    public EditorTableColumn(EditorWindowManager manager, ReadableModel model, JsonEditorEditorWindow window, JsonNode propertyNode, String propertyName, boolean isRequired, boolean holdsKey)
    {
        super();
        this.manager = manager;
        this.model = model;
        this.window = window;
        String columnName = propertyName;
        if (holdsKey)
        {
            columnName = "🔑 " + columnName;
        }
        if (isRequired)
        {
            columnName += " *";
        }
        setText(columnName);
        this.propertyName = propertyName;
        this.isRequired = isRequired;
        this.holdsKeyOfReferenceableObject = holdsKey;
        
        // every column holds one property of the array's items
        setCellValueFactory(data -> {
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
        setCellFactory(column1 -> {
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
    }
    
    
    private TableCell<JsonNodeWithPath, String> makeOpenButtonTableCell(String pathToOpen)
    {
        return new TableCell<>()
        {
            
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                JsonNodeWithPath currentNode = getTableRow().getItem();
                if (empty || item == null || currentNode == null)
                {
                    setGraphic(null);
                }
                else
                {
                    setGraphic(makeOpenButton(currentNode.getPath() + "/" + pathToOpen));
                }
            }
        };
    }
    
    private Button makeOpenButton(String pathToOpen)
    {
        Button button = new Button("Open");
        button.setOnAction(event -> {
            if (pathToOpen != null)
            {
                // if the "open" button is clicked, we want to open that node in the current window
                window.setSelectedPath(pathToOpen);
            }
        });
        button.setTooltip(TooltipHelper.makeTooltipFromJsonNode(model.getNodeForPath(pathToOpen).getNode()));
        return button;
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
    
    
    
    public String getPropertyName()
    {
        return propertyName;
    }
    
    public boolean isRequired()
    {
        return isRequired;
    }
    
    public boolean holdsKeyProperty()
    {
        return holdsKeyOfReferenceableObject;
    }
}
