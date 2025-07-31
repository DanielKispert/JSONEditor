package com.daniel.jsoneditor.model.commands;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.impl.AddNodeToArrayCommand;


public class CommandFactory
{
    private final ReadableModel readModel;
    
    private final WritableModelInternal model;
    
    public CommandFactory(ReadableModel readModel, WritableModelInternal model)
    {
        this.readModel = readModel;
        this.model = model;
    }
    
    public AddNodeToArrayCommand addNodeToArrayCommand(String path)
    {
        return new AddNodeToArrayCommand(readModel, model, path);
    }
}
