package com.daniel.jsoneditor.model.commands;

/**
 * A command adjusts the model in some way. It comes from a user action in the UI
 */
public interface Command
{
    void execute();
    
    void undo();
}
