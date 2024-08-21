package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.AutofillField;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TextTableCell extends EditorTableCell
{
    
    private final ReadableModel model;
    
    private final boolean alsoAllowNumbers;
    
    public TextTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean alsoAllowNumbers,
            boolean holdsObjectKey)
    {
        super(manager, controller, model, holdsObjectKey);
        this.model = model;
        this.alsoAllowNumbers = alsoAllowNumbers;
        HBox.setHgrow(this, Priority.ALWAYS);
    }
    
    @Override
    protected void saveValue(JsonNodeWithPath item, String propertyName, String newValue)
    {
        if (alsoAllowNumbers)
        {
            try
            {
                int valueAsInt = Integer.parseInt(newValue);
                item.setProperty(propertyName, valueAsInt);
            }
            catch (NumberFormatException e)
            {
                try
                {
                    double valueAsDouble = Double.parseDouble(newValue);
                    item.setProperty(propertyName, valueAsDouble);
                }
                catch (NumberFormatException f)
                {
                    item.setProperty(propertyName, newValue);
                }
            }
        }
        else
        {
            item.setProperty(propertyName, newValue);
        }
    }
    
    @Override
    protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            setGraphicWithResizing(null);
            createNewReferenceableObjectButton = null;
        }
        else
        {
            Pair<Boolean, List<String>> suggestions = getSuggestions();
            HBox fieldGraphic = new HBox();
            Control fill;
            if (suggestions.getValue().isEmpty())
            {
                fill = new EditorTextField(this, item);
            }
            else
            {
                fill = new AutofillField(this, item, suggestions.getValue(), !suggestions.getKey());
            }
            HBox.setHgrow(fill, Priority.ALWAYS);
            createNewReferenceableObjectButton = new Button();
            ButtonHelper.setButtonImage(createNewReferenceableObjectButton, "/icons/material/darkmode/outline_create_white_24dp.png");
            createNewReferenceableObjectButton.setOnAction(event -> handleCreateNewReferenceableObject());
            fieldGraphic.getChildren().addAll(fill, createNewReferenceableObjectButton);
            toggleCreateNewReferenceableObjectButtonVisibility();
            
            setGraphicWithResizing(fieldGraphic);
        }
    }
    
    private List<ReferenceableObjectInstance> getFittingReferenceableObjects()
    {
        return null;
        
    }
    
    private Pair<Boolean, List<String>> getSuggestions()
    {
        if (getTableRow() == null || getTableRow().getItem() == null)
        {
            return new Pair<>(false, Collections.emptyList());
        }
        
        JsonNodeWithPath parentNode = getTableRow().getItem();
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        String propertyName = column.getPropertyName();
        String path = parentNode.getPath() + "/" + propertyName;
        
        final List<String> examples = model.getStringExamplesForPath(path);
        final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
        
        List<String> suggestions;
        if (allowedValues.isEmpty())
        {
            suggestions = examples;
        }
        else
        {
            suggestions = allowedValues;
        }
        
        return new Pair<>(allowedValues.isEmpty(), suggestions);
    }
}
