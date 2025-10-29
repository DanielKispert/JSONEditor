package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class DuplicateArrayItemCommand extends BaseCommand
{
    private final String itemPath;
    
    public DuplicateArrayItemCommand(final WritableModelInternal model, final String itemPath)
    {
        super(model);
        this.itemPath = itemPath;
    }
    
    @Override
    public CommandCategory getCategory() { return CommandCategory.STRUCTURE; }
    
    @Override
    public String getLabel() { return "Duplicate Array Item"; }
    
    @Override
    public List<ModelChange> execute()
    {
        JsonNode original = model.getNodeForPath(itemPath).getNode();
        if (original == null || original.isMissingNode())
        {
            return noChanges();
        }
        // duplicate via model method (inserts at index+1)
        String parentPath = PathHelper.getParentPath(itemPath);
        int fromIndex = Integer.parseInt(SchemaHelper.getLastPathSegment(itemPath));
        model.duplicateArrayItem(itemPath);
        int newIndex = fromIndex + 1;
        String newPath = parentPath + "/" + newIndex;
        JsonNode newNode = model.getNodeForPath(newPath).getNode();
        if (newNode == null || newNode.isMissingNode())
        {
            return noChanges();
        }
        return List.of(ModelChange.add(newPath, newNode));
    }
}

