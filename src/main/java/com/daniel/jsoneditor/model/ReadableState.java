package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.observe.Subject;

public interface ReadableState
{
    
    Event getCurrentState();
    
    Subject getForObservation();
}
