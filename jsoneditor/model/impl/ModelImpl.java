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
    
    private JsonNode rootJson;
    
    private JsonNode selectedJsonNode;
    
    private String nameOfSelectedJsonNode;
    
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
    public JsonNode getRootJson()
    {
        return rootJson;
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
    public void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema)
    {
        setCurrentJSONFile(jsonFile);
        setCurrentSchemaFile(schemaFile);
        setRootJson(json);
        this.selectedJsonNode = json;
        this.nameOfSelectedJsonNode = "Root Element";
        setSchema(schema);
        setState(State.MAIN_EDITOR);
    }
    
    public void setCurrentJSONFile(File json)
    {
        this.jsonFile = json;
    }
    
    private void setCurrentSchemaFile(File schema)
    {
        this.schemaFile = schema;
    }
    
    private void setRootJson(JsonNode rootJson)
    {
        this.rootJson = rootJson;
    }
    
    private void setSchema(JsonSchema schema)
    {
        this.schema = schema;
    }
    
    public void setState(State state)
    {
        stateMachine.setState(state);
    }
    
    @Override
    public void selectJsonNode(String name, JsonNode jsonNode)
    {
        this.selectedJsonNode = jsonNode;
        this.nameOfSelectedJsonNode = name;
        setState(State.UPDATED_SELECTED_JSON_NODE);
    }
    
    @Override
    public JsonNode getSelectedJsonNode()
    {
        return selectedJsonNode;
    }
    
    @Override
    public String getNameOfSelectedJsonNode()
    {
        return nameOfSelectedJsonNode;
    }
}
