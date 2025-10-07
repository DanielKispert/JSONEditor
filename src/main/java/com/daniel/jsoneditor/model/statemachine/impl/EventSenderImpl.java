package com.daniel.jsoneditor.model.statemachine.impl;

import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.statemachine.EventSender;

import java.util.ArrayList;
import java.util.List;

public class EventSenderImpl implements EventSender
{
    
    private Event currentState;
    
    private List<Observer> observers;
    
    public EventSenderImpl()
    {
        this.observers = new ArrayList<>();
        sendEvent(new Event(EventEnum.LAUNCHING));
    }
    
    @Override
    public void registerObserver(Observer newObserver)
    {
        this.observers.add(newObserver);
        newObserver.update();
    }
    
    @Override
    public void notifyObservers()
    {
        observers.forEach(Observer::update);
    }
    
    @Override
    public void sendEvent(Event newState)
    {
        this.currentState = newState;
        notifyObservers();
    }
    
    @Override
    public Event getState()
    {
        return currentState;
    }
}
