package com.daniel.jsoneditor.model.statemachine.impl;

import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.Command;

import java.util.List;


public class Event
{
    private final String path;
    
    private final EventEnum eventEnum;
    
    // command metadata (only set for COMMAND_APPLIED)
    private final String commandLabel;
    
    private final String commandCategory;
    
    private final String commandPhase; // EXECUTE | UNDO | REDO
    
    private final List<ModelChange> changes;
    
    private final Command command; // actual command object for type-safe detection
    
    public Event(EventEnum eventEnum)
    {
        this.eventEnum = eventEnum;
        this.path = null;
        this.commandLabel = null;
        this.commandCategory = null;
        this.commandPhase = null;
        this.changes = null;
        this.command = null;
    }
    
    public Event(EventEnum eventEnum, String path)
    {
        this.eventEnum = eventEnum;
        this.path = path;
        this.commandLabel = null;
        this.commandCategory = null;
        this.commandPhase = null;
        this.changes = null;
        this.command = null;
    }
    
    /**
     * Constructor for COMMAND_APPLIED events.
     */
    public Event(EventEnum eventEnum, String commandLabel, String commandCategory, String commandPhase, List<ModelChange> changes)
    {
        this.eventEnum = eventEnum;
        this.path = null;
        this.commandLabel = commandLabel;
        this.commandCategory = commandCategory;
        this.commandPhase = commandPhase;
        this.changes = changes;
        this.command = null;
    }
    
    /**
     * Enhanced constructor for COMMAND_APPLIED events with actual command object.
     */
    public Event(EventEnum eventEnum, Command command, String commandPhase, List<ModelChange> changes)
    {
        this.eventEnum = eventEnum;
        this.path = null;
        this.commandLabel = command.getLabel();
        this.commandCategory = command.getCategory();
        this.commandPhase = commandPhase;
        this.changes = changes;
        this.command = command;
    }
    
    public EventEnum getEvent()
    {
        return eventEnum;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getCommandLabel()
    {
        return commandLabel;
    }
    
    public String getCommandCategory()
    {
        return commandCategory;
    }
    
    public String getCommandPhase()
    {
        return commandPhase;
    }
    
    public List<ModelChange> getChanges()
    {
        return changes;
    }
    
    public Command getCommand()
    {
        return command;
    }
}
