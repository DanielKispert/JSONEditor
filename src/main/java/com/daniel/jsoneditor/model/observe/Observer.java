package com.daniel.jsoneditor.model.observe;

public interface Observer
{
    void update();
    
    void observe(Subject subjectToObserve);
}
