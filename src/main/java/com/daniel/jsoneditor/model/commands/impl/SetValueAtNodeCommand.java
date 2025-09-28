package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class SetValueAtNodeCommand extends BaseCommand
{
    private final String parentPath;
    
    private final String propertyName;
    
    private final Object newValue;
    
    public SetValueAtNodeCommand(final WritableModelInternal model, final String parentPath, final String propertyName,
            final Object newValue)
    {
        super(model);
        this.parentPath = parentPath;
        this.propertyName = propertyName;
        this.newValue = newValue;
    }
    
    @Override
    public String getCategory() { return "VALUE"; }
    
    @Override
    public String getLabel() { return "Set Value"; }
    
    /**
     * Applies the value change and returns a semantic ModelChange (add/remove/replace) or empty list if no diff.
     * @return list with at most one ModelChange
     */
    @Override
    public List<ModelChange> execute()
    {
        final String fullPath = parentPath + "/" + propertyName; // parentPath may be "" for root
        final JsonNode oldNode = model.getNodeForPath(fullPath).getNode();
        model.setValueAtPath(parentPath, propertyName, newValue); // mutation
        final JsonNode newNode = model.getNodeForPath(fullPath).getNode();
        final boolean oldMissing = oldNode.isMissingNode();
        final boolean newMissing = newNode.isMissingNode();
        if (oldMissing && !newMissing)
        {
            return List.of(ModelChange.add(fullPath, newNode));
        }
        if (!oldMissing && newMissing)
        {
            return List.of(ModelChange.remove(fullPath, oldNode));
        }
        if (!oldMissing && !newMissing && !oldNode.equals(newNode))
        {
            return List.of(ModelChange.replace(fullPath, oldNode, newNode));
        }
        return noChanges();
    }
}
