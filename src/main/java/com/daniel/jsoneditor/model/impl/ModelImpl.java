package com.daniel.jsoneditor.model.impl;

import com.daniel.jsoneditor.model.settings.IdentifierSetting;
import com.daniel.jsoneditor.model.statemachine.StateMachine;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.settings.Settings;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ModelImpl implements ReadableModel, WritableModel
{
    private static final String NUMBER_REGEX = "-?\\d+(\\.\\d+)?";
    
    private final StateMachine stateMachine;
    
    private File jsonFile;
    
    private File schemaFile;
    
    private JsonNode rootJson;
    
    private JsonSchema rootSchema;
    
    private Settings settings;
    
    public ModelImpl(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
        this.settings = new Settings(null, null);
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
        sendEvent(Event.MAIN_EDITOR);
    }
    
    @Override
    public String searchForNode(String path, String value)
    {
        return NodeSearcher.findPathWithValue(getRootJson(), path, value);
    }
    
    @Override
    public void setSettings(Settings settings)
    {
        this.settings = settings;
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
    public void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index)
    {
        JsonNode selectedNode = newParent.getNode();
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
    
    @Override
    public JsonSchema getRootSchema()
    {
        return rootSchema;
    }
    
    @Override
    public Settings getSettings()
    {
        return settings;
    }
    
    @Override
    public JsonNodeWithPath getNodeForPath(String path)
    {
        return new JsonNodeWithPath(getRootJson().at(path), path);
    }
    
    @Override
    public JsonNode getSubschemaForPath(String path)
    {
        return getSubschemaNodeForPath(path).getSchemaNode();
    }
    
    @Override
    public List<String> getStringExamplesForPath(String path)
    {
        JsonNode schema = getSubschemaForPath(path);
        if (schema != null && schema.has("type") && schema.get("type").asText().equals("string"))
        {
            JsonNode examplesNode = schema.get("examples");
            if (examplesNode != null && examplesNode.isArray())
            {
                List<String> examples = new ArrayList<>();
                for (JsonNode exampleNode : examplesNode)
                {
                    if (exampleNode.isTextual())
                    {
                        examples.add(exampleNode.asText());
                    }
                }
                return examples;
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public void sortArray(String path)
    {
        JsonNodeWithPath nodeAtPath = getNodeForPath(path);
        if (nodeAtPath == null || !nodeAtPath.isArray())
        {
            return;
        }
        ArrayNode arrayNode = (ArrayNode) nodeAtPath.getNode();
        JsonNode schema = getSubschemaForPath(path);
        JsonNode itemsSchema = schema.get("items");
        String type = NodeSearcher.getTypeFromNode(itemsSchema);
        List<JsonNode> items = StreamSupport.stream(arrayNode.spliterator(), false).collect(Collectors.toList());
        // iterate over the array node and sort the items of the array alphabetically
        // the identifier of the array items (if applicable) will be used to sort the array in ascending order
        if (type != null)
        {
            switch (type)
            {
                case "object":
                    items.sort((o1, o2) -> {
                        String identifier1 = getIdentifier(nodeAtPath.getPath(), o1);
                        String identifier2 = getIdentifier(nodeAtPath.getPath(), o2);
                        if (identifier1.matches(NUMBER_REGEX))
                        {
                            if (identifier2.matches(NUMBER_REGEX))
                            {
                                Double id1 = Double.parseDouble(identifier1);
                                Double id2 = Double.parseDouble(identifier2);
                                return id1.compareTo(id2);
                            }
                            else
                            {
                                // numbers always go in front of strings
                                return 1;
                            }
                        }
                        else
                        {
                            if (identifier2.matches(NUMBER_REGEX))
                            {
                                // strings always go behind numbers
                                return -1;
                            }
                            else
                            {
                                return identifier1.compareTo(identifier2);
                            }
                        }
                    });
                    break;
                case "string":
                    items.sort(Comparator.comparing(JsonNode::asText));
                    break;
                case "number":
                    items.sort(Comparator.comparingDouble(JsonNode::asDouble));
                    break;
            }
        }
        arrayNode.removeAll();
        arrayNode.addAll(items);
        sendEvent(Event.UPDATED_JSON_STRUCTURE);
        
    }
    
    @Override
    public List<String> getAllowedStringValuesForPath(String path)
    {
        JsonNode schema = getSubschemaForPath(path);
        if (schema != null && schema.has("type") && schema.get("type").asText().equals("string"))
        {
            JsonNode examplesNode = schema.get("enum");
            if (examplesNode != null && examplesNode.isArray())
            {
                List<String> examples = new ArrayList<>();
                for (JsonNode exampleNode : examplesNode)
                {
                    if (exampleNode.isTextual())
                    {
                        examples.add(exampleNode.asText());
                    }
                }
                return examples;
            }
        }
        return Collections.emptyList();
    }
    
    public boolean canAddMoreItems(String path)
    {
        JsonNode subschema = getSubschemaForPath(path);
        if (subschema != null && subschema.get("type").asText().equals("array"))
        {
            JsonNode maxItemsNode = subschema.get("maxItems");
            if (maxItemsNode != null && maxItemsNode.isInt())
            {
                return getNodeForPath(path).getNode().size() < maxItemsNode.intValue();
            }
            else
            {
                // the node is an array but has no maxItems, so maxItems is infinite.
                return true;
            }
        }
        // either the node doesn't exist or the node is not an array
        return false;
    }
    
    @Override
    public void addNodeToArray(String selectedPath)
    {
        JsonNode itemsSchema = getSubschemaForPath(selectedPath + "/0");
        JsonNode newItem = NodeGenerator.generateNodeFromSchema(itemsSchema);
        JsonNodeWithPath parent = getNodeForPath(selectedPath);
        if (parent.isArray())
        {
            ((ArrayNode) parent.getNode()).add(newItem);
            sendEvent(Event.UPDATED_SELECTED_JSON_NODE);
        }
    }
    
    @Override
    public void duplicateArrayItem(String pathToItemToDuplicate)
    {
        JsonNodeWithPath parentArray = getNodeForPath(SchemaHelper.getParentPath(pathToItemToDuplicate));
        
        if (parentArray != null && parentArray.getNode().isArray())
        {
            ArrayNode arrayNode = (ArrayNode) parentArray.getNode();
            
            // Get the index of the item to be cloned
            int indexToClone = Integer.parseInt(SchemaHelper.getLastPathSegment(pathToItemToDuplicate));
            
            // Clone the item at indexToClone + 1
            JsonNode clonedNode = arrayNode.get(indexToClone).deepCopy();
            
            // Insert the cloned item at indexToClone + 1
            arrayNode.insert(indexToClone + 1, clonedNode);
            sendEvent(Event.UPDATED_JSON_STRUCTURE);
        }
    }
    
    @Override
    public void removeNode(String path)
    {
        String[] pathComponents = path.split("/");
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
        sendEvent(Event.REMOVED_SELECTED_JSON_NODE);
    }
    
    private JsonSchema getSubschemaNodeForPath(String path)
    {
        // this will be an array like ["", "addresses" "1" "street"]
        String[] pathParts = path.split("/");
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
    
    @Override
    public String getIdentifier(String pathOfParentNode, JsonNode childNode)
    {
        for (IdentifierSetting identifier : settings.getIdentifiers())
        {
            Pair<String, String> formattedQueryPath = NodeSearcher.formatQueryPath(identifier.getIdentifier());
            if (pathOfParentNode.contains(formattedQueryPath.getKey()))
            {
                return childNode.at(formattedQueryPath.getValue()).asText();
            }
        }
        return null;
    }
}
