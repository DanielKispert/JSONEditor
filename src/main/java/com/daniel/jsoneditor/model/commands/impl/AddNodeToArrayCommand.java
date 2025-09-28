package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class AddNodeToArrayCommand extends BaseCommand
{
    
    private final String pathToArray;
    
    private String pathOfAddedNode;
    
    public AddNodeToArrayCommand(WritableModelInternal model, String pathToArray)
    {
        super(model);
        this.pathToArray = pathToArray;
    }
    

    public List<ModelChange> execute() {
        
        int addedIndex = model.addNodeToArray(pathToArray);
        if (addedIndex == -1) {
            return noChanges();
        }
        pathOfAddedNode = pathToArray + "/" + addedIndex;
        // keep legacy event behavior so existing UI still updates
        model.sendEvent(new Event(EventEnum.ADDED_ITEM_TO_ARRAY_FROM_ARRAY, pathOfAddedNode));
        return List.of(ModelChange.add(pathOfAddedNode, newNode));
    }
}
