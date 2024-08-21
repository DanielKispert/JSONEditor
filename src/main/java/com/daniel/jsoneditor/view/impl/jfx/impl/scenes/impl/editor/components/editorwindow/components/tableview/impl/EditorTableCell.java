package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CreateNewReferenceableObjectButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.util.Objects;


public abstract class EditorTableCell extends TableCell<JsonNodeWithPath, String>
{
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    protected final boolean holdsKeyOfReferenceableObject;
    
    private String previouslyCommittedValue;
    
    protected CreateNewReferenceableObjectButton createNewReferenceableObjectButton;
    
    public EditorTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean holdsObjectKey)
    {
        this.manager = manager;
        this.controller = controller;
        this.model = model;
        this.holdsKeyOfReferenceableObject = holdsObjectKey;
        setMaxWidth(Double.MAX_VALUE);
        Platform.runLater(() -> {
            TableColumn<JsonNodeWithPath, String> column = getTableColumn();
            if (column != null)
            {
                ((EditorTableColumn) column).updatePrefWidth();
            }
        });
        
    }
    
    protected String getValue()
    {
        return previouslyCommittedValue;
    }
    
    protected boolean holdsKeyOfReferenceableObjectThatDoesNotExist()
    {
        if (!((EditorTableColumn) getTableColumn()).holdsObjectKeysOfReferences())
        {
            return false;
        }
        
        String cellValue = getValue();
        if (cellValue == null || cellValue.isEmpty())
        {
            return false;
        }
        
        String objectReferencingKey = getObjectReferencingKey();
        if (objectReferencingKey == null) return false;
        
        return model.getReferenceableObjectInstance(objectReferencingKey, getValue()) == null;
    }
    
    private String getObjectReferencingKey()
    {
        // we can assume that the parent item is a referenceToObject because the previous condition was true
        JsonNodeWithPath parentItem = getTableRow().getItem();
        if (parentItem == null)
        {
            return null;
        }
        ReferenceToObject referenceToObject = model.getReferenceToObject(parentItem.getPath());
        if (referenceToObject == null)
        {
            return null;
        }
        return referenceToObject.getObjectReferencingKeyOfInstance(parentItem.getNode());
    }
    
    protected void handleCreateNewReferenceableObject()
    {
        String objectReferencingKey = getObjectReferencingKey();
        ReferenceableObject referenceableObject = model.getReferenceableObjectByReferencingKey(objectReferencingKey);
        if (referenceableObject != null)
        {
            // create a new referenceable object instance from the model
            controller.createNewReferenceableObjectNodeWithKey(referenceableObject.getPath(), getValue());
        }
    }
    
    protected final void setGraphicWithResizing(Node node)
    {
        setGraphic(node);
    }
    
    @Override
    public final void commitEdit(String newValue)
    {
        if (Objects.equals(newValue, previouslyCommittedValue))
        {
            return; //in case the value is already committed, we don't need to change anything
        }
        super.commitEdit(newValue);
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        if (getTableRow() == null || getTableRow().getItem() == null)
        {
            return;
        }
        // from this point on we likely will save the value, so we remember it for next time
        previouslyCommittedValue = newValue;
        JsonNodeWithPath item = getTableRow().getItem();
        String propertyName = column.getPropertyName();
        JsonNode jsonNode = item.getNode().get(propertyName);
        
        // if we are editing an object key we check if we need to update all references, too
        // we do this before we edit the actual node so that we don't have to remember the before-key
        if (controller.getSettingsController().renameReferencesWhenRenamingObject() && holdsKeyOfReferenceableObject)
        {
            // we need to get the nodes of all references and update them as well
            for (ReferenceToObjectInstance referenceToObjectInstance : model.getReferencesToObjectForPath(item.getPath()))
            {
                JsonNodeWithPath referencingNode = model.getNodeForPath(referenceToObjectInstance.getPath());
                if (newValue.isEmpty() && !column.isRequired())
                {
                    referencingNode.removeProperty(referenceToObjectInstance.getReference().getObjectKey());
                }
                else
                {
                    referencingNode.setProperty(referenceToObjectInstance.getReference().getObjectKey(), newValue);
                }
            }
        }
        // display the button to create a new object if applicable
        toggleCreateNewReferenceableObjectButtonVisibility();
        
        //save in the JSON we're editing
        if (jsonNode != null && jsonNode.isValueNode())
        {
            if (newValue.isEmpty() && !column.isRequired())
            {
                item.removeProperty(propertyName);
            }
            else
            {
                saveValue(item, propertyName, newValue);
            }
        }
        else
        {
            // the node does not yet exist, so we do not need to remove it if we save an empty value. We just need to save something
            // in case there is a value.
            if (!newValue.isEmpty() || column.isRequired())
            {
                saveValue(item, propertyName, newValue);
            }
            
        }
        manager.updateNavbarRepresentation(item.getPath());
        column.updatePrefWidth();
    }
    
    protected void toggleCreateNewReferenceableObjectButtonVisibility()
    {
        if (createNewReferenceableObjectButton == null)
        {
            return;
        }
        if (holdsKeyOfReferenceableObjectThatDoesNotExist())
        {
            createNewReferenceableObjectButton.setVisible(true);
            createNewReferenceableObjectButton.setManaged(true);
        }
        else
        {
            createNewReferenceableObjectButton.setVisible(false);
            createNewReferenceableObjectButton.setManaged(false);
        }
    }
    
    
    
    protected abstract void saveValue(JsonNodeWithPath item, String propertyName, String newValue);
}
