package com.daniel.jsoneditor.model.commands.impl;

import java.util.ArrayList;
import java.util.List;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.ReferenceableObjectCommand;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Duplicates an object at pathToItemToDuplicate and updates the referencing node at referencePath to point to the new key.
 * Undo removes the duplicated object and restores the referencing node snapshot (via REPLACE inversion).
 */
public class DuplicateReferenceAndLinkCommand extends BaseCommand implements ReferenceableObjectCommand
{
    private final String referencePath;
    private final String pathToItemToDuplicate;
    private String createdObjectPath;
    
    public DuplicateReferenceAndLinkCommand(final WritableModelInternal model, final String referencePath, final String pathToItemToDuplicate)
    {
        super(model);
        this.referencePath = referencePath;
        this.pathToItemToDuplicate = pathToItemToDuplicate;
    }
    
    @Override
    public String getCategory() { return "STRUCTURE"; }
    
    @Override
    public String getLabel() { return "Duplicate Ref & Link"; }
    
    @Override
    public List<ModelChange> execute()
    {
        final JsonNode oldRefSnapshot = model.getNodeForPath(referencePath).getNode().deepCopy();
        JsonNodeWithPath itemNode = model.getNodeForPath(pathToItemToDuplicate);
        if (itemNode == null || itemNode.getNode().isMissingNode())
        {
            return noChanges();
        }
        final String parentPath = PathHelper.getParentPath(pathToItemToDuplicate);
        final int originalIndex = Integer.parseInt(PathHelper.getLastPathSegment(pathToItemToDuplicate));
        final String newItemPath = parentPath + "/" + (originalIndex + 1);
        this.createdObjectPath = newItemPath;
        model.duplicateNodeAndLink(referencePath, pathToItemToDuplicate);
        JsonNode newItem = model.getNodeForPath(newItemPath).getNode();
        if (newItem == null || newItem.isMissingNode())
        {
            return noChanges();
        }
        final JsonNode newRefSnapshot = model.getNodeForPath(referencePath).getNode().deepCopy();
        List<ModelChange> out = new ArrayList<>();
        out.add(ModelChange.add(newItemPath, newItem));
        if (!oldRefSnapshot.equals(newRefSnapshot))
        {
            out.add(ModelChange.replace(referencePath, oldRefSnapshot, newRefSnapshot));
        }
        
        return out;
    }
    
    @Override
    public String getCreatedObjectPath()
    {
        return createdObjectPath;
    }
}
