package com.daniel.jsoneditor.model.commands.impl;

import java.util.ArrayList;
import java.util.List;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;

public class RemoveNodesCommand extends BaseCommand
{
    private final List<String> paths;
    
    public RemoveNodesCommand(final WritableModelInternal model, final List<String> paths)
    {
        super(model);
        this.paths = paths == null ? List.of() : paths;
    }
    
    @Override
    public CommandCategory getCategory() { return CommandCategory.STRUCTURE; }
    
    @Override
    public String getLabel() { return "Remove Multiple Nodes"; }
    
    @Override
    public List<ModelChange> execute()
    {
        if (paths.isEmpty())
        {
            return noChanges();
        }
        List<ModelChange> changes = new ArrayList<>();
        // capture old nodes first (before mutation)
        for (String p : paths)
        {
            JsonNode oldNode = model.getNodeForPath(p).getNode();
            if (!oldNode.isMissingNode())
            {
                changes.add(ModelChange.remove(p, oldNode));
            }
        }
        if (changes.isEmpty())
        {
            return noChanges();
        }
        // remove in reverse depth order to avoid index shifting issues (longer paths first)
        model.removeNodes(paths);
        return changes;
    }
}

