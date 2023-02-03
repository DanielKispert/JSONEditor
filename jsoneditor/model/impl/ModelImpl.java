package jsoneditor.model.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.WritableModel;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.State;

import java.io.File;

public class ModelImpl implements ReadableModel, WritableModel
{
    private final StateMachine stateMachine;
    
    private File jsonFile;
    
    private File schemaFile;
    
    private JsonNode json;
    
    private JsonSchema schema;
    
    public ModelImpl(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
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
    
    @Override
    public Subject getForObservation()
    {
        return stateMachine;
    }
    
    @Override
    public void setCurrentJSONFile(File json)
    {
        this.jsonFile = json;
    }
    
    @Override
    public void setCurrentSchemaFile(File schema)
    {
        this.schemaFile = schema;
    }
    
    @Override
    public void setJson(JsonNode json)
    {
        this.json = json;
    }
    
    @Override
    public void setSchema(JsonSchema schema)
    {
        this.schema = schema;
    }
    
    @Override
    public void setState(State state)
    {
        stateMachine.setState(state);
    }
}
