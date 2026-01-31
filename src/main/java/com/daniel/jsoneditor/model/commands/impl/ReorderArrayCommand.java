package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;


public class ReorderArrayCommand extends BaseArrayReorderCommand
{
    private final List<Integer> newIndices;
    
    /**
     * @param model Model to operate on
     * @param path Path to the array node
     * @param newIndices List of indices representing the new order (e.g., [2,0,1] means element at index 2 moves to position 0)
     */
    public ReorderArrayCommand(final WritableModelInternal model, final String path, final List<Integer> newIndices)
    {
        super(model, path);
        this.newIndices = newIndices;
    }
    
    @Override
    public String getLabel()
    {
        return "Reorder Array";
    }
    
    @Override
    protected boolean performReorder(ArrayNode arrayNode)
    {
        if (arrayNode.size() != newIndices.size())
        {
            return false;
        }
        
        final ArrayNode reorderedArray = JsonNodeFactory.instance.arrayNode();
        for (Integer index : newIndices)
        {
            if (index < 0 || index >= arrayNode.size())
            {
                return false;
            }
            reorderedArray.add(arrayNode.get(index).deepCopy());
        }
        
        arrayNode.removeAll();
        arrayNode.addAll(reorderedArray);
        
        return true;
    }
}
