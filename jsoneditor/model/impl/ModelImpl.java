package jsoneditor.model.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
        return jsonFile;
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
    public void selectJsonNode(String path)
    {
        selectJsonNodeAndSubschema(getNodeForPath(path));
        sendEvent(Event.UPDATED_SELECTED_JSON_NODE);
    }
    
    @Override
    public void moveItemToIndex(JsonNodeWithPath item, int index)
    {
        JsonNode selectedNode = selectedJsonNode.getNode();
        JsonNode itemNode = item.getNode();
        if (selectedNode.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) selectedNode;
            for (int i = 0; i < arrayNode.size(); i++)
            {
                JsonNode arrayItem = arrayNode.get(i);
                if (arrayItem.equals(itemNode))
                {
                    arrayNode.remove(i);
                    arrayNode.insert(index, itemNode);
                    sendEvent(Event.MOVED_CHILD_OF_SELECTED_JSON_NODE);
                    break;
                }
            }
        }

        
    }
    
    private void selectJsonNodeAndSubschema(JsonNodeWithPath nodeWithPath)
    {
        this.selectedJsonNode = nodeWithPath;
        this.subschemaForSelectedNode = getSubschemaNodeForPath(nodeWithPath);
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
    public JsonNodeWithPath getNodeForPath(String path)
    {
        return new JsonNodeWithPath(getRootJson().at(path), path);
    }
    
    @Override
    public boolean canAddMoreItems()
    {
        JsonNode node = getSchemaNodeOfSelectedNode();
        JsonNode maxItemsNode = node.get("maxItems");
        if (maxItemsNode != null && maxItemsNode.isInt())
        {
            return selectedJsonNode.getNode().size() < maxItemsNode.intValue();
        }
        return true;
    }
    
    @Override
    public boolean editingAnArray()
    {
        return selectedJsonNode.getNode().isArray();
    }
    
    @Override
    public boolean editingAnObject()
    {
        return selectedJsonNode.getNode().isObject();
    }
    
    @Override
    public JsonNode getSchemaNodeOfSelectedNode()
    {
        return SchemaHelper.getSchemaNodeResolvingRefs(rootSchema, subschemaForSelectedNode);
    }
    
    @Override
    public void removeNodeFromSelectedArray(JsonNode node)
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
    public void addNodeToSelectedArray()
    {
        JsonNodeWithPath selectedArray = selectedJsonNode;
        JsonNode newItem = makeNodeThatFitsIntoArraySchema();
        ((ArrayNode) selectedArray.getNode()).add(newItem);
        sendEvent(Event.UPDATED_SELECTED_JSON_NODE);
    }
    
    public JsonNode makeNodeThatFitsIntoArraySchema()
    {
        JsonNode schema = getSchemaNodeOfSelectedNode();
        JsonNodeFactory factory = JsonNodeFactory.instance;
        String type = schema.get("items").get("type").asText().toLowerCase();
        switch (type)
        {
            case "object":
                return factory.objectNode();
            case "array":
                return factory.arrayNode();
            case "string":
                return factory.textNode("");
            case "number":
            case "integer":
                return factory.numberNode(0);
            case "boolean":
                return factory.booleanNode(true);
        }
        return null;
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
    
    public JsonSchema getSubschemaNodeForPath(JsonNodeWithPath node)
    {
        // this will be an array like ["", "addresses" "1" "street"]
        String[] pathParts = node.getPath().split("/");
        // this is the root schema, we want the schema that validates only the node that is given by the path
        JsonNode subNode = rootSchema.getSchemaNode();
        for (String part : pathParts)
        {
            if (!part.isEmpty())
            {
                subNode = resolveRef(subNode);
                JsonNode typeNode = subNode.get("type");
                if (typeNode.isTextual())
                {
                    String type = typeNode.textValue();
                    if ("object".equalsIgnoreCase(type))
                    {
                        // go into the "properties" node and then get the object that's referenced by the key
                        subNode = subNode.get("properties").get(part);
                    }
                    else if ("array".equalsIgnoreCase(type))
                    {
                        subNode = subNode.get("items");
                    }
                }
            }
        }
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(subNode);
    }
    
    private JsonNode resolveRef(JsonNode nodeWithRef)
    {
        JsonNode ref = nodeWithRef.get("$ref");
        if (ref != null)
        {
            return rootSchema.getRefSchemaNode(ref.textValue());
        }
        return nodeWithRef;
    }
}
