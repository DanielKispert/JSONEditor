package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Command to set a complete node at a specific path, replacing whatever was there before
 */
public class SetNodeCommand extends BaseCommand
{
    private final String path;
    
    private final JsonNode newNode;
    
    /**
     * @param model the writable model
     * @param path the path where to set the node
     * @param newNode the node to set at the path
     */
    public SetNodeCommand(final WritableModelInternal model, final String path, final JsonNode newNode)
    {
        super(model);
        this.path = path;
        this.newNode = newNode;
    }
    
    @Override
    public String getCategory() { return "NODE"; }
    
    @Override
    public String getLabel() { return "Set Node"; }
    
    /**
     * Sets the node at the path and returns the semantic ModelChange
     * @return list with at most one ModelChange
     */
    @Override
    public List<ModelChange> execute()
    {
        final JsonNode oldNode = model.getNodeForPath(path).getNode();
        model.setNode(path, newNode);
        final JsonNode actualNewNode = model.getNodeForPath(path).getNode();
        
        final boolean oldMissing = oldNode.isMissingNode();
        final boolean newMissing = actualNewNode.isMissingNode();
        
        if (oldMissing && !newMissing)
        {
            return List.of(ModelChange.add(path, actualNewNode));
        }
        if (!oldMissing && newMissing)
        {
            return List.of(ModelChange.remove(path, oldNode));
        }
        if (!oldMissing && !oldNode.equals(actualNewNode))
        {
            return List.of(ModelChange.replace(path, oldNode, actualNewNode));
        }
        
        return List.of(); // no change
    }
}
