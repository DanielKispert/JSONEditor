package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.ReferenceableObjectCommand;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class CreateReferenceableObjectCommand extends BaseCommand implements ReferenceableObjectCommand
{
    private final String referenceableObjectPath;
    private final String key;
    private String createdObjectPath;
    
    public CreateReferenceableObjectCommand(final WritableModelInternal model, final String referenceableObjectPath, final String key)
    {
        super(model);
        this.referenceableObjectPath = referenceableObjectPath;
        this.key = key;
    }
    
    @Override
    public String getCategory() { return "STRUCTURE"; }
    
    @Override
    public String getLabel() { return "Create Referenceable Object"; }
    
    @Override
    public List<ModelChange> execute()
    {
        ReferenceableObject refObj = ReferenceHelper.getReferenceableObjectOfPath(model, referenceableObjectPath);
        if (refObj == null)
        {
            return noChanges();
        }
        String path = refObj.getPath();
        JsonNode newItem = model.makeArrayNode(path);
        int addedIndex = model.addNodeToArray(path, newItem);
        if (addedIndex == -1)
        {
            return noChanges();
        }
        String newNodePath = path + "/" + addedIndex;
        this.createdObjectPath = newNodePath;
        ReferenceHelper.setKeyOfInstance(model, refObj, newNodePath, key);
        
        return List.of(ModelChange.add(newNodePath, model.getNodeForPath(newNodePath).getNode()));
    }
    
    @Override
    public String getCreatedObjectPath()
    {
        return createdObjectPath;
    }
}
