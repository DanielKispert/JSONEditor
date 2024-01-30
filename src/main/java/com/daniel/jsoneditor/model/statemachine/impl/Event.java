package com.daniel.jsoneditor.model.statemachine.impl;

public class Event
{
    private final String path;
    
    private final EventEnum eventEnum;
    
    public Event(EventEnum eventEnum)
    {
        this.eventEnum = eventEnum;
        this.path = null;
    }
    
    public Event(EventEnum eventEnum, String path)
    {
        this.eventEnum = eventEnum;
        this.path = path;
    }
    
    public EventEnum getEvent()
    {
        return eventEnum;
    }
    
    public String getPath()
    {
        return path;
    }
    
}
