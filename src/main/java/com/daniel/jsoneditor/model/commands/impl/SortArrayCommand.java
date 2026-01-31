package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.fasterxml.jackson.databind.node.ArrayNode;


public class SortArrayCommand extends BaseArrayReorderCommand
{
    public SortArrayCommand(final WritableModelInternal model, final String path)
    {
        super(model, path);
    }
    
    @Override
    public String getLabel()
    {
        return "Sort Array";
    }
    
    @Override
    protected boolean performReorder(ArrayNode arrayNode)
    {
        model.sortArray(path);
        return true;
    }
}

