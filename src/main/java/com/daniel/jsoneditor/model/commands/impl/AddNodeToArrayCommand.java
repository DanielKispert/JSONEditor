package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;


public class AddNodeToArrayCommand extends BaseCommand
{
    
    private final String pathToArray;
    
    private String pathOfAddedNode;
    
    public AddNodeToArrayCommand(ReadableModel readModel, WritableModelInternal model, String pathToArray)
    {
        super(readModel, model);
        this.pathToArray = pathToArray;
    }
    
    @Override
    public void execute()
    {
        int addedIndex = model.addNodeToArray(pathToArray);
        pathOfAddedNode = pathToArray + "/" + addedIndex;
    }
    
    @Override
    public void undo()
    {
        model.removeNode(pathOfAddedNode);
    }
}
