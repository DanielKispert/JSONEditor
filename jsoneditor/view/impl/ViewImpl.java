package jsoneditor.view.impl;

import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.State;
import jsoneditor.view.View;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.UIHandlerImpl;

import java.util.ArrayList;
import java.util.List;

public class ViewImpl implements View
{
    private List<Subject> subjects;
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    private final UIHandler uiHandler;
    
    public ViewImpl(ReadableModel model, Controller controller)
    {
        this.subjects = new ArrayList<>();
        this.uiHandler = new UIHandlerImpl();
        this.controller = controller;
        this.model = model;
    }
    
    
    @Override
    public void update()
    {
        State newState = model.getCurrentState();
        switch (newState)
        {
            case READ_JSON_AND_SCHEMA:
                uiHandler.startUI();
                break;
        }
    
    }
    
    @Override
    public void observe(Subject subjectToObserve)
    {
        subjectToObserve.registerObserver(this);
        subjects.add(subjectToObserve);
    }
    
    
    
}
