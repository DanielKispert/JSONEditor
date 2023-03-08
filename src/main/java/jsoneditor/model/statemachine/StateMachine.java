package jsoneditor.model.statemachine;

import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.Event;

public interface StateMachine extends Subject
{
    void setState(Event newState);
    
    Event getState();
}
