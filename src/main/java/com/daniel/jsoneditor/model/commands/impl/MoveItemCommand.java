package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.CommandCategory;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;

public class MoveItemCommand extends BaseCommand
{
    private final String itemPath;
    private final int targetIndex;
    
    public MoveItemCommand(final WritableModelInternal model, final String itemPath, final int targetIndex)
    {
        super(model);
        this.itemPath = itemPath;
        this.targetIndex = targetIndex;
    }
    
    @Override
    public CommandCategory getCategory() { return CommandCategory.STRUCTURE; }
    
    @Override
    public String getLabel() { return "Move Item"; }
    
    @Override
    public List<ModelChange> execute()
    {
        JsonNodeWithPath item = model.getNodeForPath(itemPath);
        if (item == null || item.getNode().isMissingNode())
        {
            return noChanges();
        }
        String parentPath = PathHelper.getParentPath(itemPath);
        JsonNode parentNode = model.getNodeForPath(parentPath).getNode();
        if (parentNode == null || !parentNode.isArray())
        {
            return noChanges();
        }
        int fromIndex = Integer.parseInt(SchemaHelper.getLastPathSegment(itemPath));
        if (fromIndex == targetIndex)
        {
            return noChanges();
        }
        ArrayNode array = (ArrayNode) parentNode;
        if (targetIndex < 0 || targetIndex >= array.size())
        {
            return noChanges();
        }
        JsonNode moved = array.remove(fromIndex);
        int adjustedTarget = targetIndex;
        if (targetIndex > fromIndex)
        {
            adjustedTarget = targetIndex - 1;
        }
        array.insert(adjustedTarget, moved);
        return List.of(ModelChange.move(parentPath, fromIndex, adjustedTarget));
    }
}

