package com.daniel.jsoneditor.model.commands;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.impl.AddNodeToArrayCommand;
import com.daniel.jsoneditor.model.commands.impl.SetValueAtNodeCommand;


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
}
