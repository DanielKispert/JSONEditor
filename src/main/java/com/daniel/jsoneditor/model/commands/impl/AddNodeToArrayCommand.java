package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;


public class AddNodeToArrayCommand extends BaseCommand
{
    
    private final String path;
    
    public AddNodeToArrayCommand(ReadableModel readModel, WritableModelInternal model, String path)
    {
        super(readModel, model);
        this.path = path;
    }
    
    @Override
    public void execute()
    {
        model.addNodeToArray(path);
    }
    
    @Override
    public void undo()
    {
        // doublecheck this
        model.removeNode(path);
    }
}
