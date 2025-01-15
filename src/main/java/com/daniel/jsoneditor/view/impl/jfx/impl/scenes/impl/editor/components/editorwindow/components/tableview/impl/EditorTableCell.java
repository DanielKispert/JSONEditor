package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CreateNewReferenceableObjectButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.AutofillField;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import com.daniel.jsoneditor.view.impl.jfx.popups.FittingObjectsPopup;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract class representing a custom table cell in the editor table.
 * This class provides functionality for handling the display and editing of JSON node values within a table cell.
 * It also manages the display of a popup for selecting referenceable objects and handles the creation of new referenceable objects.
 */
public abstract class EditorTableCell extends TableCell<JsonNodeWithPath, String>
{
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    protected final boolean holdsKeyOfReferenceableObject;
    
    private String previouslyCommittedValue;
    
    protected String displayedValue;
    
    protected CreateNewReferenceableObjectButton createNewReferenceableObjectButton;
    
    private final FittingObjectsPopup fittingObjectsPopup;
    
    protected Control currentTextEnterGraphic;
    
    public EditorTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean holdsObjectKey)
    {
        this.manager = manager;
        this.controller = controller;
        this.model = model;
        this.holdsKeyOfReferenceableObject = holdsObjectKey;
        this.fittingObjectsPopup = new FittingObjectsPopup(model, this::onFittingObjectSelected, this::onDuplicateItemButtonClicked);
        setMaxWidth(Double.MAX_VALUE);
        Platform.runLater(() -> {
            TableColumn<JsonNodeWithPath, String> column = getTableColumn();
            if (column != null)
            {
                ((EditorTableColumn) column).updatePrefWidth();
            }
        });
    }
    
    private void onDuplicateItemButtonClicked(ReferenceableObjectInstance selectedItem)
    {
        controller.duplicateReferenceableObjectForLinking(getTableRow().getItem().getPath(), selectedItem.getPath());
    }
    
    private void onFittingObjectSelected(ReferenceableObjectInstance selectedItem)
    {
        if (currentTextEnterGraphic instanceof AutofillField)
        {
            ((AutofillField) currentTextEnterGraphic).setValue(selectedItem.getKey());
        }
        else if (currentTextEnterGraphic instanceof EditorTextField)
        {
            ((EditorTextField) currentTextEnterGraphic).setText(selectedItem.getKey());
        }
        commitEdit(selectedItem.getKey());
    }
    
    public void contentFocusChanged(boolean isNowFocused)
    {
        if (isNowFocused)
        {
            showPopup();
        }
        else
        {
            hidePopup();
        }
    }
    
    private void showPopup()
    {
        List<ReferenceableObjectInstance> fittingObjects = getFittingReferenceableObjects();
        if (fittingObjects.isEmpty())
        {
            return;
        }
        Bounds cellBounds = localToScreen(getBoundsInLocal());
        // we give the position of the BOTTOM left corner because the popup needs to calculate the one for the top left
        fittingObjectsPopup.setPopupPosition(getScene().getWindow(), cellBounds.getMinX(), cellBounds.getMinY());
        fittingObjectsPopup.setItems(fittingObjects);
    }
    
    private void hidePopup()
    {
        fittingObjectsPopup.hide();
    }
    
    protected String getValue()
    {
        return displayedValue;
    }
    
    public List<ReferenceableObjectInstance> getFittingReferenceableObjects()
    {
        if (!((EditorTableColumn) getTableColumn()).holdsObjectKeysOfReferences())
        {
            return new ArrayList<>();
        }
        List<ReferenceableObjectInstance> fittingObjects = new ArrayList<>();
        String objectReferencingKey = getObjectReferencingKey();
        ReferenceableObject referenceableObject = model.getReferenceableObjectByReferencingKey(objectReferencingKey);
        for (ReferenceableObjectInstance instance : model.getReferenceableObjectInstances(referenceableObject))
        {
                String value = getValue();
                if (value == null || instance.getKey().contains(value))
                {
                    fittingObjects.add(instance);
                }
        }
        return fittingObjects;
        
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
        
        ReferenceableObject objectWithKey = model.getReferenceableObjectByReferencingKey(getObjectReferencingKey());
        if (objectWithKey == null)
        {
            return false; //if no referenceable object exists, we can't create a new instance of it. Don't show the button
        }
        // we made sure the object exists, but does an instance with the key exist?
        return model.getReferenceableObjectInstanceWithKey(objectWithKey, getValue()) == null;
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
    
    public void onUserChangedText(String enteredText)
    {
        if (!Objects.equals(enteredText, displayedValue))
        {
            displayedValue = enteredText;
            toggleCreateNewReferenceableObjectButtonVisibility();
            fittingObjectsPopup.setItems(getFittingReferenceableObjects());
        }
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
