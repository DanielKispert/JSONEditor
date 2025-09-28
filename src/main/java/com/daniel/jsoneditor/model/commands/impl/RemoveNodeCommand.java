package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class RemoveNodeCommand extends BaseCommand
{
    private final String path;
    
    public RemoveNodeCommand(final WritableModelInternal model, final String path)
    {
        super(model);
        this.path = path;
    }
    
    @Override
    public String getCategory() { return "STRUCTURE"; }
    
    @Override
    public String getLabel() { return "Remove Node"; }
    
    @Override
    public List<ModelChange> execute()
    {
        final JsonNode oldNode = model.getNodeForPath(path).getNode();
        if (oldNode.isMissingNode())
        {
            return noChanges();
        }
        model.removeNode(path);
        return List.of(ModelChange.remove(path, oldNode));
    }
}

