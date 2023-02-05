package jsoneditor.model.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.WritableModel;
import jsoneditor.model.json.JsonNodeWithPath;
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
    
    private JsonNodeWithPath selectedJsonNode;
    
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
        this.selectedJsonNode = new JsonNodeWithPath(json, "Root Element", "");
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
    public void selectJsonNode(JsonNodeWithPath nodeWithPath)
    {
        this.selectedJsonNode = nodeWithPath;
        setState(State.UPDATED_SELECTED_JSON_NODE);
    }
    
    @Override
    public JsonNodeWithPath getSelectedJsonNode()
    {
        return selectedJsonNode;
    }
    
    @Override
    public JsonSchema getSchema()
    {
        return schema;
    }
    
    
    @Override
    public boolean canAddMoreItems()
    {
        return false;
    }
    
    @Override
    public void removeNodeFromArray(JsonNode node)
    {
        JsonNode selectedNode = selectedJsonNode.getNode();
        if (JsonNodeType.ARRAY.equals(selectedNode.getNodeType()))
        {
            ArrayNode arrayNode = (ArrayNode) selectedNode;
            for (int i = 0; i < arrayNode.size(); i++)
            {
                if (arrayNode.get(i).equals(node))
                {
                    arrayNode.remove(i);
                    break;
                }
            }
        }
        setState(State.UPDATED_SELECTED_JSON_NODE);
    }
    
    @Override
    public void removeSelectedNode()
    {
        selectedJsonNode.
    
    }
}
