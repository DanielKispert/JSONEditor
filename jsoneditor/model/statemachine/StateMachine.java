package jsoneditor.model.statemachine;

import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.State;

public interface StateMachine extends Subject
{
    void setState(State newState);
    
    State getState();
}
