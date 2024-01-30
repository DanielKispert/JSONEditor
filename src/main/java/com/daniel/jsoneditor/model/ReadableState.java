package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.statemachine.impl.Event;


public interface ReadableState
{
    
    Event getLatestEvent();
    
    Subject getForObservation();
}
