package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

<<<<<<< Updated upstream
public class AddNodeToArrayCommand extends BaseCommand
{
    
    private final String pathToArray;
    
    private String pathOfAddedNode;
    
    public AddNodeToArrayCommand(WritableModelInternal model, String pathToArray)
    {
        super(model);
        this.pathToArray = pathToArray;
=======
public class AddNodeToArrayCommand extends BaseCommand {
    private final String path; // array path

    public AddNodeToArrayCommand(ReadableModel readModel, WritableModelInternal model, String path) {
        super(readModel, model);
        this.path = path;
>>>>>>> Stashed changes
    }

    @Override
<<<<<<< Updated upstream
    public void execute()
    {
        int addedIndex = model.addNodeToArray(pathToArray);
        pathOfAddedNode = pathToArray + "/" + addedIndex;
    }
    
    @Override
    public void undo()
    {
        model.removeNode(pathOfAddedNode);
=======
    public List<ModelChange> execute() {
        // create new node from schema
        JsonNode newNode = readModel.makeArrayNode(path);
        int index = model.addNodeToArray(path, newNode);
        if (index == -1) {
            return noChanges();
        }
        String newItemPath = path + "/" + index;
        // keep legacy event behavior so existing UI still updates
        model.sendEvent(new Event(EventEnum.ADDED_ITEM_TO_ARRAY_FROM_ARRAY, newItemPath));
        return List.of(ModelChange.add(newItemPath, newNode));
>>>>>>> Stashed changes
    }
}
