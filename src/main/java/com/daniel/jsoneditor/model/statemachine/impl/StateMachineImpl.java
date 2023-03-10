package com.daniel.jsoneditor.model.statemachine.impl;

import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.List;

public class StateMachineImpl implements StateMachine
{
    
    private Event currentState;
    
    private List<Observer> observers;
    
    public StateMachineImpl()
    {
        this.observers = new ArrayList<>();
        setState(Event.LAUNCHING);
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
    public void setState(Event newState)
    {
        System.out.println("State Machine entering state " + newState);
        this.currentState = newState;
        notifyObservers();
    }
    
    @Override
    public Event getState()
    {
        return currentState;
    }
}
