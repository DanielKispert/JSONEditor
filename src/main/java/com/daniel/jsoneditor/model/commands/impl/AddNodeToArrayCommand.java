package com.daniel.jsoneditor.model.commands.impl;

import java.util.List;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;


public class AddNodeToArrayCommand extends BaseCommand
{
    private final String pathToArray;
    
    public AddNodeToArrayCommand(final WritableModelInternal model, final String pathToArray)
    {
        super(model);
        this.pathToArray = pathToArray;
    }
    
    @Override
    public CommandCategory getCategory() { return CommandCategory.STRUCTURE; }
    
    @Override
    public String getLabel() { return "Add Array Element"; }
    
    /**
     * Creates a new array element (schema‑driven), appends it and returns an ADD change or empty if append failed.
     *
     * @return list with at most one ADD ModelChange
     */
    @Override
    public List<ModelChange> execute()
    {
        final JsonNode newNode = model.makeArrayNode(pathToArray);
        final int addedIndex = model.addNodeToArray(pathToArray, newNode);
        if (addedIndex == -1)
        {
            return noChanges();
        }
        // kept for potential future reference (e.g. follow-up commands)
        final String pathOfAddedNode = pathToArray + "/" + addedIndex;
        return List.of(ModelChange.add(pathOfAddedNode, newNode));
    }
}
