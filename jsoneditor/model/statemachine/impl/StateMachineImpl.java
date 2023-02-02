package jsoneditor.model.statemachine.impl;

import jsoneditor.model.observe.Observer;
import jsoneditor.model.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.List;

public class StateMachineImpl implements StateMachine
{
    
    private State currentState;
    
    private List<Observer> observers;
    
    public StateMachineImpl()
    {
        this.currentState = State.READ_JSON_AND_SCHEMA;
        this.observers = new ArrayList<>();
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
    public void setState(State newState)
    {
        this.currentState = newState;
        notifyObservers();
    }
    
    @Override
    public State getState()
    {
        return currentState;
    }
}
