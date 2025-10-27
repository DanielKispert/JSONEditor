package com.daniel.jsoneditor.model.commands;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.impl.AddNodeToArrayCommand;
import com.daniel.jsoneditor.model.commands.impl.CreateReferenceableObjectCommand;
import com.daniel.jsoneditor.model.commands.impl.DuplicateArrayItemCommand;
import com.daniel.jsoneditor.model.commands.impl.DuplicateReferenceAndLinkCommand;
import com.daniel.jsoneditor.model.commands.impl.MoveItemCommand;
import com.daniel.jsoneditor.model.commands.impl.RemoveNodesCommand;
import com.daniel.jsoneditor.model.commands.impl.SetNodeCommand;
import com.daniel.jsoneditor.model.commands.impl.SetValueAtNodeCommand;
import com.daniel.jsoneditor.model.commands.impl.SortArrayCommand;
import com.fasterxml.jackson.databind.JsonNode;


public class CommandFactory
{
    
    private final WritableModelInternal model;
    
    public CommandFactory(WritableModelInternal model)
    {
        this.model = model;
    }
    
    public AddNodeToArrayCommand addNodeToArrayCommand(String path)
    {
        return new AddNodeToArrayCommand(model, path);
    }
    
    public SetValueAtNodeCommand setValueAtNodeCommand(String parentPath, String propertyName, Object value)
    {
        return new SetValueAtNodeCommand(model, parentPath, propertyName, value);
    }
    
    public RemoveNodesCommand removeNodesCommand(java.util.List<String> paths)
    {
        return new RemoveNodesCommand(model, paths);
    }
    
    public SortArrayCommand sortArrayCommand(String path)
    {
        return new SortArrayCommand(model, path);
    }
    
    public MoveItemCommand moveItemCommand(String itemPath, int targetIndex)
    {
        return new MoveItemCommand(model, itemPath, targetIndex);
    }
    
    public CreateReferenceableObjectCommand createReferenceableObjectCommand(String refObjPath, String key)
    {
        return new CreateReferenceableObjectCommand(model, refObjPath, key);
    }
    
    public DuplicateArrayItemCommand duplicateArrayItemCommand(String itemPath)
    {
        return new DuplicateArrayItemCommand(model, itemPath);
    }
    
    public DuplicateReferenceAndLinkCommand duplicateReferenceAndLinkCommand(String referencePath, String pathToDuplicate)
    {
        return new DuplicateReferenceAndLinkCommand(model, referencePath, pathToDuplicate);
    }
    
    public SetNodeCommand setNodeCommand(String path, JsonNode newNode)
    {
        return new SetNodeCommand(model, path, newNode);
    }
}
