package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.buttons.CreateNewReferenceableObjectButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableRow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.EditorTableColumn;
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
import javafx.scene.control.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(EditorTableCell.class);
    
    protected final Controller controller;
    
    private final EditorWindowManager manager;
    
    private final ReadableModel model;
    
    protected final boolean holdsKeyOfReferenceableObject;
    
    protected String currentValue; // What the user is currently seeing/typing
    protected String committedValue; // What was last saved to the model (also serves as previouslyCommittedValue)
    
    protected CreateNewReferenceableObjectButton createNewReferenceableObjectButton;
    
    private final FittingObjectsPopup fittingObjectsPopup;
    
    protected Control currentTextEnterGraphic;
    
    public EditorTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean holdsObjectKey)
    {
        this.manager = manager;
        this.controller = controller;
        this.model = model;
        this.holdsKeyOfReferenceableObject = holdsObjectKey;
        this.createNewReferenceableObjectButton = new CreateNewReferenceableObjectButton(event -> handleCreateNewReferenceableObject());
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
    
    private String getCellValue()
    {
        return currentValue;
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
                String value = getCellValue();
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
        
        String cellValue = getCellValue();
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
        return model.getReferenceableObjectInstanceWithKey(objectWithKey, getCellValue()) == null;
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
    
    private void handleCreateNewReferenceableObject()
    {
        String objectReferencingKey = getObjectReferencingKey();
        ReferenceableObject referenceableObject = model.getReferenceableObjectByReferencingKey(objectReferencingKey);
        if (referenceableObject != null)
        {
            // create a new referenceable object instance from the model
            controller.createNewReferenceableObjectNodeWithKey(referenceableObject.getPath(), getCellValue());
        }
    }
    
    protected final void setGraphicWithResizing(Node node)
    {
        setGraphic(node);
    }
    
    @Override
    public final void commitEdit(String newValue)
    {
        if (Objects.equals(newValue, committedValue))
        {
            return; // in case the value is already committed, we don't need to change anything
        }
        super.commitEdit(newValue);
        
        // Update our state tracking - both values sync when committed
        currentValue = newValue;
        committedValue = newValue;
        
        EditorTableColumn column = ((EditorTableColumn) getTableColumn());
        TableRow<JsonNodeWithPath> tableRow = getTableRow();
        if (tableRow == null || tableRow.getItem() == null)
        {
            return;
        }
        
        JsonNodeWithPath item = tableRow.getItem();
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
                String pathToChange = referencingNode.getPath() + referenceToObjectInstance.getReference().getObjectKey();
                controller.setValueAtPath(pathToChange, (!newValue.isEmpty() || column.isRequired()) ? newValue : null);
            }
        }
        
        //save in the JSON we're editing
        if (jsonNode != null && jsonNode.isValueNode())
        {
            if (newValue.isEmpty() && !column.isRequired())
            {
                controller.setValueAtPath(item.getPath() + "/" + propertyName, null);
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
        // update the follow reference or open column button of this row, if existing
        if (tableRow instanceof EditorTableRow)
        {
            ((EditorTableRow) tableRow).updateFollowRefOrOpenItemButton();
        }
    }
    
    public void onUserChangedText(String enteredText)
    {
        if (!Objects.equals(enteredText, currentValue))
        {
            currentValue = enteredText;
            toggleCreateNewReferenceableObjectButtonVisibility();
            fittingObjectsPopup.setItems(getFittingReferenceableObjects());
        }
    }
    
    protected void toggleCreateNewReferenceableObjectButtonVisibility()
    {
        if (!createNewReferenceableObjectButton.isVisible() && holdsKeyOfReferenceableObjectThatDoesNotExist())
        {
            createNewReferenceableObjectButton.setVisible(true);
            createNewReferenceableObjectButton.setManaged(true);
        }
        else if (createNewReferenceableObjectButton.isVisible() && !holdsKeyOfReferenceableObjectThatDoesNotExist())
        {
            createNewReferenceableObjectButton.setVisible(false);
            createNewReferenceableObjectButton.setManaged(false);
        }
    }
    
    
    protected abstract void saveValue(JsonNodeWithPath item, String propertyName, String newValue);
}
