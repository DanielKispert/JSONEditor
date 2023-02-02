package jsoneditor.model.impl;

import jsoneditor.model.ReadableModel;
import jsoneditor.model.WritableModel;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.State;

import java.io.File;

public class ModelImpl implements ReadableModel, WritableModel
{
    private final StateMachine stateMachine;
    
    public ModelImpl(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
    }
    
    @Override
    public void setCurrentJSONFile()
    {
    
    }
    
    @Override
    public void setCurrentSchemaFile()
    {
    
    }
    
    @Override
    public File getCurrentJSONFile()
    {
        return null;
    }
    
    @Override
    public File getCurrentSchemaFile()
    {
        return null;
    }
    
    @Override
    public State getCurrentState()
    {
        return stateMachine.getState();
    }
}
