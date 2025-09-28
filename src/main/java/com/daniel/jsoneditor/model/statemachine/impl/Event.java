package com.daniel.jsoneditor.model.statemachine.impl;

import com.daniel.jsoneditor.model.changes.ModelChange;

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
    
    public Event(EventEnum eventEnum)
    {
        this.eventEnum = eventEnum;
        this.path = null;
        this.commandLabel = null;
        this.commandCategory = null;
        this.commandPhase = null;
        this.changes = null;
    }
    
    public Event(EventEnum eventEnum, String path)
    {
        this.eventEnum = eventEnum;
        this.path = path;
        this.commandLabel = null;
        this.commandCategory = null;
        this.commandPhase = null;
        this.changes = null;
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
}
