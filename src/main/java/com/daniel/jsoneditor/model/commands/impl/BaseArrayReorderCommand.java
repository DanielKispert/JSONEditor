package com.daniel.jsoneditor.model.commands.impl;

import java.util.List;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


public abstract class BaseArrayReorderCommand extends BaseCommand
{
    protected final String path;
    
    protected BaseArrayReorderCommand(final WritableModelInternal model, final String path)
    {
        super(model);
        this.path = path;
    }
    
    @Override
    public CommandCategory getCategory()
    {
        return CommandCategory.STRUCTURE;
    }
    
    @Override
    public List<ModelChange> execute()
    {
        final JsonNode node = model.getNodeForPath(path).getNode();
        if (node == null || !node.isArray())
        {
            return noChanges();
        }
        
        final ArrayNode arrayNode = (ArrayNode) node;
        final ArrayNode oldSnapshot = arrayNode.deepCopy();
        
        if (!performReorder(arrayNode))
        {
            return noChanges();
        }
        
        final ArrayNode newSnapshot = arrayNode.deepCopy();
        
        if (oldSnapshot.equals(newSnapshot))
        {
            return noChanges();
        }
        
        return List.of(ModelChange.sort(path, oldSnapshot, newSnapshot));
    }
    
    /**
     * @param arrayNode The array node to reorder (will be mutated)
     * @return true if reordering was performed, false if operation should be cancelled
     */
    protected abstract boolean performReorder(ArrayNode arrayNode);
}
