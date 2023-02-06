package jsoneditor.model.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.WritableModel;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.json.schema.SchemaHelper;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.Event;

import java.io.File;

public class ModelImpl implements ReadableModel, WritableModel
{
    private final StateMachine stateMachine;
    
    private File jsonFile;
    
    private File schemaFile;
    
    private JsonNode rootJson;
    
    private JsonNodeWithPath selectedJsonNode;
    
    private JsonSchema subschemaForSelectedNode;
    
    private JsonSchema rootSchema;
    
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
    public Event getCurrentState()
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
        setRootSchema(schema);
        selectJsonNodeAndSubschema(new JsonNodeWithPath(json, ""));
        sendEvent(Event.MAIN_EDITOR);
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
    
    private void setRootSchema(JsonSchema rootSchema)
    {
        this.rootSchema = rootSchema;
    }
    
    public void sendEvent(Event state)
    {
        stateMachine.setState(state);
    }
    
    @Override
    public void selectJsonNode(JsonNodeWithPath nodeWithPath)
    {
        selectJsonNodeAndSubschema(nodeWithPath);
        sendEvent(Event.UPDATED_SELECTED_JSON_NODE);
    }
    
    private void selectJsonNodeAndSubschema(JsonNodeWithPath nodeWithPath)
    {
        this.selectedJsonNode = nodeWithPath;
        this.subschemaForSelectedNode = SchemaHelper.getSubschemaNodeForPath(rootSchema, nodeWithPath);
    }
    
    @Override
    public JsonNodeWithPath getSelectedJsonNode()
    {
        return selectedJsonNode;
    }
    
    @Override
    public JsonSchema getRootSchema()
    {
        return rootSchema;
    }
    
    
    @Override
    public boolean canAddMoreItems()
    {
    
        Integer maxItems = SchemaHelper.getMaxItems(subschemaForSelectedNode);
        if (maxItems != null)
        {
            return selectedJsonNode.getNode().size() < maxItems;
        }
        return true;
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
        sendEvent(Event.UPDATED_SELECTED_JSON_NODE);
    }
    
    @Override
    public void removeSelectedNode()
    {
        String selectedPath = selectedJsonNode.getPath();
        String[] pathComponents = selectedPath.split("/");
        JsonNode parentNode = rootJson;
        // we go to the parent node of the one we want to remove
        for (int i = 1; i < pathComponents.length - 1; i++)
        {
            String nextPath = pathComponents[i];
            if (parentNode.isArray())
            {
                parentNode = parentNode.get(Integer.parseInt(nextPath));
            }
            else
            {
                parentNode = parentNode.get(nextPath);
            }
        }
        // Get the name of the target node
        String targetNodeName = pathComponents[pathComponents.length - 1];
        if (parentNode.isObject())
        {
            // Remove the target JsonNode from its parent ObjectNode
            ((ObjectNode) parentNode).remove(targetNodeName);
        }
        else if (parentNode.isArray())
        {
            // try to parse targetNodeName into an integer (for an index)
            int index = Integer.parseInt(targetNodeName);
            ((ArrayNode) parentNode).remove(index);
        } else {
            // TODO make this prettier
            return;
        }
        // the parent node becomes the new selected node
        String parentPath = "";
        int lastSlashIndex = selectedPath.lastIndexOf("/");
        if (lastSlashIndex != -1)
        {
            parentPath = selectedPath.substring(0, lastSlashIndex);
        }
        selectJsonNodeAndSubschema(new JsonNodeWithPath(parentNode, parentPath));
        sendEvent(Event.REMOVED_SELECTED_JSON_NODE);
        
        
    }
}
