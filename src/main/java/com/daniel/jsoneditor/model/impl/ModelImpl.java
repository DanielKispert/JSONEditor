package com.daniel.jsoneditor.model.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.CommandFactory;
import com.daniel.jsoneditor.model.impl.graph.NodeGraph;
import com.daniel.jsoneditor.model.impl.graph.NodeGraphCreator;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.settings.IdentifierSetting;
import com.daniel.jsoneditor.model.statemachine.EventSender;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.RenameKeyDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.settings.Settings;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ModelImpl implements ReadableModel, WritableModelInternal
{
    private static final Logger logger = LoggerFactory.getLogger(ModelImpl.class);
    
    private static final String NUMBER_REGEX = "-?\\d+(\\.\\d+)?";
    
    private final EventSender eventSender;
    
    private File jsonFile;
    
    private File schemaFile;
    
    private JsonNode rootJson;
    
    private JsonSchema rootSchema;
    
    private Settings settings;
    
    public ModelImpl(EventSender eventSender)
    {
        this.eventSender = eventSender;
        this.settings = new Settings(null, null);
    }
    
    @Override
    public CommandFactory getCommandFactory()
    {
        return new CommandFactory(this);
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
    public Event getLatestEvent()
    {
        return eventSender.getState();
    }
    
    @Override
    public Subject getForObservation()
    {
        return eventSender;
    }
    
    @Override
    public void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema)
    {
        setCurrentJSONFile(jsonFile);
        setCurrentSchemaFile(schemaFile);
        setRootJson(json);
        setRootSchema(schema);
        sendEvent(new Event(EventEnum.MAIN_EDITOR));
    }
    
    @Override
    public void resetRootNode(JsonNode jsonNode)
    {
        setRootJson(jsonNode);
        // Notify UI that the entire JSON structure has been updated
        sendEvent(new Event(EventEnum.RELOADED_JSON_FROM_DISK, ""));
        sendEvent(new Event(EventEnum.RESET_SUCCESSFUL));
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
        eventSender.sendEvent(state);
    }
    
    @Override
    public void moveItemToIndex(JsonNodeWithPath item, int index)
    {
        JsonNodeWithPath parent = getNodeForPath(PathHelper.getParentPath(item.getPath()));
        ArrayNode arrayNode = (ArrayNode) parent.getNode();
        JsonNode itemNode = item.getNode();
        
        for (int i = 0; i < arrayNode.size(); i++)
        {
            if (arrayNode.get(i).equals(itemNode))
            {
                arrayNode.remove(i);
                arrayNode.insert(index, itemNode);
                break;
            }
        }
    }
    
    @Override
    public void setValueAtPath(String parentPath, String propertyName, Object value)
    {
        JsonNodeWithPath nodeWithPath = getNodeForPath(parentPath);
        logger.debug("Setting value at path {} with property {} to {}", parentPath, propertyName, value);
        nodeWithPath.setProperty(propertyName, value);
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
        // the root node is reachable under "" and not null.
        if (path == null)
        {
            return null;
        }
        return new JsonNodeWithPath(getRootJson().at(path), path);
    }
    
    @Override
    public JsonNode getExportStructureForNodes(List<String> paths)
    {
        return NodeStructureDelegate.getExportStructureForNodes(this, paths);
    }
    
    @Override
    public List<String> getDependentPaths(JsonNodeWithPath node)
    {
        // the dependent nodes of an item are the references that it or its child objects have
        List<String> referencedNodes = new ArrayList<>();
        collectReferencesRecursively(node, referencedNodes);
        return referencedNodes;
    }
    
    @Override
    public List<ReferenceableObject> getReferenceableObjects()
    {
        List<ReferenceableObject> referenceableObjects = new ArrayList<>();
        JsonNode rootSchema = getRootSchema().getSchemaNode();
        JsonNode objectsArray = rootSchema.get("referenceableObjects");
        if (objectsArray != null && objectsArray.isArray())
        {
            for (JsonNode item : objectsArray)
            {
                String referencingKey = item.get("referencingKey").asText();
                String path = item.get("path").asText();
                String key = item.get("key").asText();
                referenceableObjects.add(new ReferenceableObject(referencingKey, path, key));
            }
        }
        return referenceableObjects;
    }
    
    @Override
    public List<ReferenceableObjectInstance> getReferenceableObjectInstances()
    {
        List<ReferenceableObjectInstance> items = new ArrayList<>();
        for (ReferenceableObject object : getReferenceableObjects())
        {
            items.addAll(ReferenceHelper.getReferenceableObjectInstances(this, object));
        }
        return items;
    }
    
    @Override
    public List<ReferenceableObjectInstance> getReferenceableObjectInstances(ReferenceableObject object)
    {
        return ReferenceHelper.getReferenceableObjectInstances(this, object);
    }
    
    @Override
    public List<ReferenceableObjectInstance> getInstancesOfReferenceableObjectAtPath(String referenceableObjectPath)
    {
        for (ReferenceableObject object : getReferenceableObjects())
        {
            if (NodeSearcher.formatQueryPath(referenceableObjectPath).getKey().equals(object.getPath()))
            {
                return ReferenceHelper.getReferenceableObjectInstances(this, object);
            }
        }
        return new ArrayList<>();
    }
    
    private void collectReferencesRecursively(JsonNodeWithPath node, List<String> referencedNodes)
    {
        String referencePath = ReferenceHelper.resolveReference(node, this);
        if (referencePath != null)
        {
            if (referencedNodes.contains(referencePath))
            {
                // if the referenced nodes already contain the reference path then we found a circular dependency (a -> b -> a and so on)
                // in this case we skip collecting references for this node and don't add it because it already happened before
                return;
            }
            // this node is a dependency
            referencedNodes.add(referencePath);
            // we also need to add all of its dependencies
            collectReferencesRecursively(getNodeForPath(referencePath), referencedNodes);
        }
        else
        {
            // if the node itself doesn't have a reference, we check if any of its children have a reference
            if (node.isObject())
            {
                node.getNode().fields().forEachRemaining(entry -> {
                    JsonNodeWithPath childNode = new JsonNodeWithPath(entry.getValue(), node.getPath() + "/" + entry.getKey());
                    collectReferencesRecursively(childNode, referencedNodes);
                });
            }
            else if (node.isArray())
            {
                int index = 0;
                for (JsonNode child : node.getNode())
                {
                    String path = node.getPath() + "/" + index;
                    JsonNodeWithPath childNode = new JsonNodeWithPath(child, path);
                    collectReferencesRecursively(childNode, referencedNodes);
                    index++;
                }
            }
        }
        
    }
    
    @Override
    public List<String> getStringExamplesForPath(String path)
    {
        JsonNode schema = getSubschemaForPath(path).getSchemaNode();
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
        JsonNode schema = getSubschemaForPath(path).getSchemaNode();
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
                        if (identifier1 == null || identifier2 == null)
                        {
                            return 0;
                        }
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
    }
    
    @Override
    public List<String> getAllowedStringValuesForPath(String path)
    {
        JsonNode schema = getSubschemaForPath(path).getSchemaNode();
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
        JsonNode subschema = getSubschemaForPath(path).getSchemaNode();
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
    public ReferenceToObject getReferenceToObject(String path)
    {
        List<ReferenceToObject> references = ReferenceHelper.getReferenceToObjectNodes(this);
        for (ReferenceToObject reference : references)
        {
            if (PathHelper.pathsMatch(path, reference.getPath()))
            {
                return reference;
            }
        }
        return null;
    }
    
    @Override
    public ReferenceableObject getReferenceableObject(String path)
    {
        return ReferenceHelper.getReferenceableObjectOfPath(this, path);
    }
    
    @Override
    public NodeGraph getJsonAsGraph(String path)
    {
        return NodeGraphCreator.createGraph(this, path);
    }
    
    @Override
    public List<ReferenceToObjectInstance> getReferencesToObjectForPath(String path)
    {
        return ReferenceHelper.getReferencesOfObject(this, path);
    }
    
    @Override
    public int addNodeToArray(String selectedPath)
    {
        JsonNode newItem = makeArrayNode(selectedPath);
        return addNodeToArray(selectedPath, newItem);
    }
    
    @Override
    public JsonNode makeArrayNode(String selectedPath)
    {
        JsonNode itemsSchema = getSubschemaForPath(selectedPath + "/0").getSchemaNode();
        return NodeGenerator.generateNodeFromSchema(itemsSchema);
    }
    
    @Override
    public int addNodeToArray(String arrayPath, JsonNode nodeToAdd)
    {
        JsonNodeWithPath parent = getNodeForPath(arrayPath);
        if (parent.isArray())
        {
            // we need the parents size to get the index of the newly added item
            int indexOfNewItem = parent.getNode().size();
            ((ArrayNode) parent.getNode()).add(nodeToAdd);
            return indexOfNewItem;
            
        }
        return -1;
    }
    
    @Override
    public void duplicateArrayItem(String pathToItemToDuplicate)
    {
        duplicateItem(pathToItemToDuplicate);
    }
    
    @Override
    public void duplicateNodeAndLink(String referencePath, String pathToItemToDuplicate)
    {
        JsonNodeWithPath itemToDuplicate = getNodeForPath(pathToItemToDuplicate);
        if (itemToDuplicate == null) {
            return;
        }

        // Retrieve the parent node of the reference
        JsonNodeWithPath referencingNode = getNodeForPath(referencePath);
        if (referencingNode == null || !referencingNode.isObject()) {
            return;
        }
        ReferenceToObject reference = getReferenceToObject(referencePath);

        // duplicate the node
        String clonedPath = duplicateItem(pathToItemToDuplicate);

        JsonNodeWithPath clonedNode = getNodeForPath(clonedPath);

        String newKeyName = ReferenceHelper.getReferenceableObjectOfPath(this, clonedPath).getKeyOfInstance(clonedNode.getNode());

        //set the objectKey of the reference to the key of the object
        ReferenceHelper.setObjectKeyOfInstance(this, reference, referencePath, newKeyName);
    }

    private String duplicateItem(String pathToItemToDuplicate)
    {
        JsonNodeWithPath itemToDuplicate = getNodeForPath(pathToItemToDuplicate);

        JsonNodeWithPath parentArray = getNodeForPath(PathHelper.getParentPath(pathToItemToDuplicate));

        if (parentArray == null || !parentArray.getNode().isArray())
        {
            return null;
        }
        ArrayNode arrayNode = (ArrayNode) parentArray.getNode();

        // Get the index of the item to be cloned so that we can insert the next item at that index + 1
        int indexToClone = Integer.parseInt(SchemaHelper.getLastPathSegment(pathToItemToDuplicate));

        JsonNode clonedNode = itemToDuplicate.getNode().deepCopy();
        // Insert the cloned item at indexToClone + 1
        arrayNode.insert(indexToClone + 1, clonedNode);

        String clonedPath = PathHelper.getParentPath(itemToDuplicate.getPath()) + "/" + (indexToClone + 1);

        //check if the cloned node is a referenceable object, if yes then we want to offer to change its name
        ReferenceableObject object = ReferenceHelper.getReferenceableObjectOfPath(this, clonedPath);
        if (object != null)
        {
            String currentKey = object.getKeyOfInstance(itemToDuplicate.getNode());
            new RenameKeyDialog(currentKey).showAndWait().ifPresent(
                    newKey -> ReferenceHelper.setKeyOfInstance(this, object, clonedPath, newKey));
        }
        return clonedPath;
    }

    @Override
    public void removeNodes(List<String> paths)
    {
        // dirty hack: remove the last item first to avoid removing unintended items
        // this will break once we add sorting
        for (int i = paths.size() - 1; i >= 0; i--)
        {
            removeOrSetNode(paths.get(i), null);
        }
    }

    @Override
    public void removeNode(String path)
    {
        removeOrSetNode(path, null);
    }
    
    /**
     * changes the JSON structure by setting the defined content at the defined path. If you want to notify the UI of these changes, set notifyUI to true.
     */
    private void removeOrSetNode(String path, JsonNode content)
    {
        if (path == null || path.isEmpty())
        {
            // we are attempting to insert at the root node
            setRootJson(content);
            return;
        }
        String parentPath = PathHelper.getParentPath(path);
        String targetNodeName = PathHelper.getLastPathSegment(path);
        JsonNode parentNode = getNodeForPath(parentPath).getNode();
        if (parentNode.isObject())
        {
            if (content == null)
            {
                ((ObjectNode) parentNode).remove(targetNodeName);
            }
            else
            {
                ((ObjectNode) parentNode).set(targetNodeName, content);
            }
        }
        else if (parentNode.isArray())
        {
            // we try to parse targetNodeName into an integer (for an index)
            int index = Integer.parseInt(targetNodeName);
            int parentSize = parentNode.size();
            if (content == null)
            {
                ((ArrayNode) parentNode).remove(index);
            }
            else
            {
                if (index >= parentSize)
                {
                    //we target a non-existing index, meaning we need to add this node to the end of the array
                    ((ArrayNode) parentNode).add(content);
                }
                else
                {
                    // override an existing index
                    ((ArrayNode) parentNode).set(index, content);
                }
            }
        }
    }
    
    @Override
    public ReferenceableObjectInstance getReferenceableObjectInstanceWithKey(ReferenceableObject object, String key)
    {
        if (object == null)
        {
            return null;
        }
        for (ReferenceableObjectInstance instance : ReferenceHelper.getReferenceableObjectInstances(this, object))
        {
            if (instance.getKey().equals(key))
            {
                return instance;
            }
        }
        return null;
    }
    
    @Override
    public ReferenceableObject getReferenceableObjectByReferencingKey(String referencingKey)
    {
        return ReferenceHelper.getReferenceableObject(this, referencingKey);
    }
    
    @Override
    public void setNode(String path, JsonNode content)
    {
        removeOrSetNode(path, content);
    }

    @Override
    public JsonSchema getSubschemaForPath(String path)
    {
        if (path == null || path.isEmpty())
        {
            return rootSchema;
        }
        String[] pathParts = path.split("/");
        JsonNode subNode = rootSchema.getSchemaNode();
        for (String part : pathParts)
        {
            if (!part.isEmpty())
            {
                // guard against missing schema branches to avoid NPE (test adding to non-array /notArray)
                if (subNode == null)
                {
                    break; // remaining parts impossible -> fallback below
                }
                subNode = resolveRef(subNode);
                if (subNode == null)
                {
                    break;
                }
                JsonNode typeNode = subNode.get("type");
                if (typeNode != null && typeNode.isTextual())
                {
                    String type = typeNode.textValue();
                    if ("object".equalsIgnoreCase(type))
                    {
                        JsonNode props = subNode.get("properties");
                        if (props != null)
                        {
                            subNode = props.get(part);
                        }
                        else
                        {
                            subNode = null; // invalid branch
                        }
                    }
                    else if ("array".equalsIgnoreCase(type))
                    {
                        subNode = subNode.get("items");
                    }
                }
            }
        }
        if (subNode == null)
        {
            // fallback minimal schema (type null) so callers still receive a JsonSchema object
            ObjectNode fallback = JsonNodeFactory.instance.objectNode();
            fallback.put("type", "null");
            subNode = fallback;
        }
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(subNode);
    }
    
    private JsonNode resolveRef(JsonNode nodeWithRef)
    {
        if (nodeWithRef == null)
        {
            return null; // guard: missing schema path
        }
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
