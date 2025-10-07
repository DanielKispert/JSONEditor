package com.daniel.jsoneditor.model.statemachine;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.observe.Subject;

public interface EventSender extends Subject
{
    void sendEvent(Event newState);
    
    Event getState();
}
