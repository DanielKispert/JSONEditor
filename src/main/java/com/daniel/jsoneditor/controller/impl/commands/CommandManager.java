package com.daniel.jsoneditor.controller.impl.commands;

import java.util.ArrayDeque;
import java.util.Deque;

import com.daniel.jsoneditor.model.commands.Command;


public class CommandManager
{
    private final Deque<Command> undoStack = new ArrayDeque<>();
    
    private final Deque<Command> redoStack = new ArrayDeque<>();
    
    public void executeCommand(Command cmd)
    {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }
    
    public void undo()
    {
        if (!undoStack.isEmpty())
        {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }
    
    public void redo()
    {
        if (!redoStack.isEmpty())
        {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }
}
