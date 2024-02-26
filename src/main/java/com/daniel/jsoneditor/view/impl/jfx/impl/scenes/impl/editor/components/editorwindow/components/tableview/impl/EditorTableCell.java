package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import java.util.Objects;
import java.util.function.Consumer;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.TableCell;


public abstract class EditorTableCell extends TableCell<JsonNodeWithPath, String>
{
    
    private final EditorWindowManager manager;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    protected final boolean holdsKeyOfReferenceableObject;
    
    private String previouslyCommittedValue;
    
    public EditorTableCell(EditorWindowManager manager, Controller controller, ReadableModel model, boolean holdsObjectKey)
    {
        this.manager = manager;
        this.controller = controller;
        this.model = model;
        this.holdsKeyOfReferenceableObject = holdsObjectKey;
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
    }
    
    protected abstract void saveValue(JsonNodeWithPath item, String propertyName, String newValue);
    
}
