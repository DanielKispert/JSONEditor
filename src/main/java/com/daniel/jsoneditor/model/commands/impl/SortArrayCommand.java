package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;

public class SortArrayCommand extends BaseCommand
{
    private final String path;
    
    public SortArrayCommand(final WritableModelInternal model, final String path)
    {
        super(model);
        this.path = path;
    }
    
    @Override
    public CommandCategory getCategory() { return CommandCategory.STRUCTURE; }
    
    @Override
    public String getLabel() { return "Sort Array"; }
    
    @Override
    public List<ModelChange> execute()
    {
        JsonNode node = model.getNodeForPath(path).getNode();
        if (node == null || !node.isArray())
        {
            return noChanges();
        }
        ArrayNode oldSnapshot = ((ArrayNode) node).deepCopy();
        model.sortArray(path); // mutates array and fires legacy event
        JsonNode newNode = model.getNodeForPath(path).getNode();
        if (!newNode.isArray())
        {
            return noChanges();
        }
        ArrayNode newSnapshot = ((ArrayNode) newNode).deepCopy();
        // if identical order, no change
        if (oldSnapshot.equals(newSnapshot))
        {
            return noChanges();
        }
        return List.of(ModelChange.sort(path, oldSnapshot, newSnapshot));
    }
}

