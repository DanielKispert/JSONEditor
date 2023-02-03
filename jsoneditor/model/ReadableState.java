package jsoneditor.model;

import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.State;

public interface ReadableState
{
    
    State getCurrentState();
    
    Subject getForObservation();
}
