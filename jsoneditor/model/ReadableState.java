package jsoneditor.model;

import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.Event;

public interface ReadableState
{
    
    Event getCurrentState();
    
    Subject getForObservation();
}
