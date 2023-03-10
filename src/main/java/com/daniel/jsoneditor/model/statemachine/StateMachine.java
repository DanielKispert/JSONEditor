package com.daniel.jsoneditor.model.statemachine;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.observe.Subject;

public interface StateMachine extends Subject
{
    void setState(Event newState);
    
    Event getState();
}
