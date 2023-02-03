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
        this.observers = new ArrayList<>();
        setState(State.LAUNCHING);
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
        System.out.println("State Machine entering state " + newState);
        this.currentState = newState;
        notifyObservers();
    }
    
    @Override
    public State getState()
    {
        return currentState;
    }
}
