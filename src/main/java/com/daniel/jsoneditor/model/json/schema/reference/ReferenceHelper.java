package com.daniel.jsoneditor.model.json.schema.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReferenceHelper
{
    private static final Logger logger = LoggerFactory.getLogger(ReferenceHelper.class);
    
    public static String resolveReference(ReadableModel model, ReferenceToObjectInstance reference)
    {
        return resolveReference(model.getNodeForPath(reference.getPath()), model);
    }
    
    /**
     * @return the path of the ReferenceableObject that this node references, if it is a ReferenceToObject, otherwise null
     */
    public static String resolveReference(JsonNodeWithPath node, ReadableModel model)
    {
        if (node == null || model == null)
        {
            return null;
        }
        ReferenceToObject reference = model.getReferenceToObject(node.getPath());
        if (reference == null)
        {
            return null;
        }
        String objectReferencingKey = node.getNode().at(reference.getObjectReferencingKey()).asText();
        String objectKey = node.getNode().at(reference.getObjectKey()).asText();
        ReferenceableObject object = getReferenceableObject(model, objectReferencingKey);
        if (object == null)
        {
            logger.warn("Could not find a referenceable object for reference " + node.getDisplayName());
            return null;
        }
        JsonNodeWithPath objectNode = model.getNodeForPath(object.getPath());
        if (objectNode.isArray())
        {
            int index = 0;
            for (JsonNode item : objectNode.getNode())
            {
                if (objectKey.equals(object.getKeyOfInstance(item)))
                {
                    return objectNode.getPath() + "/" + index;
                }
                index++;
            }
            return null;
        }
        else
        {
            return objectNode.getPath();
        }
    }
    
    public static ReferenceableObject getReferenceableObject(ReadableModel model, String referencingKey)
    {
        if (referencingKey == null)
        {
            return null;
        }
        for (ReferenceableObject object : model.getReferenceableObjects())
        {
            if (object.getReferencingKey().equals(referencingKey))
            {
                return object;
            }
        }
        return null;
    }
    
    /**
     * goes through the currently loaded json and returns all nodes that are instances of this referenceable object
     */
    public static List<ReferenceableObjectInstance> getReferenceableObjectInstances(
            ReadableModel model,
            ReferenceableObject referenceableObject)
    {
        if (referenceableObject == null)
        {
            return new ArrayList<>();
        }
        JsonNodeWithPath objectInstance = model.getNodeForPath(referenceableObject.getPath());
        if (objectInstance.isArray())
        {
            List<ReferenceableObjectInstance> instances = new ArrayList<>();
            JsonNode arrayNode = objectInstance.getNode();
            for (int index = 0; index < arrayNode.size(); index++)
            {
                String itemPath = objectInstance.getPath() + "/" + index;
                
                // the array items are the referenceable objects
                instances.add(new ReferenceableObjectInstance(model, referenceableObject, model.getNodeForPath(itemPath)));
            }
            return instances;
        }
        else
        {
            // the referenceable object is the object itself, so we get its key
            return Collections.singletonList(new ReferenceableObjectInstance(model, referenceableObject, objectInstance));
        }
    }
    
    /**
     * returns the referenceable object that is at this path
     */
    public static ReferenceableObject getReferenceableObjectOfPath(ReadableModel model, String path)
    {
        // we first check whether the object itself is the referenceableObject. But only if we're not an array. If we're an array we can't be a referenceable object (for reasons)
        JsonNodeWithPath node = model.getNodeForPath(path);
        List<ReferenceableObject> referenceableObjects = model.getReferenceableObjects();
        if (node != null)
        {
            for (ReferenceableObject object : referenceableObjects)
            {
                if (object.getPath().equals(path))
                {
                    return object;
                }
            }
        }
        // then we need to check whether the path leads to an array item (because if the referenceable object is an array, its items will be the "real" referenceable objects)
        String parentPath = PathHelper.getParentPath(path);
        if (parentPath == null)
        {
            return null;
        }
        JsonNodeWithPath parent = model.getNodeForPath(parentPath);
        if (parent != null && parent.isArray())
        {
            // if our parent is an array, we check if it matches one of the referenceable objects
            for (ReferenceableObject object : referenceableObjects)
            {
                if (object.getPath().equals(parent.getPath()))
                {
                    return object;
                }
            }
        }
        return null;
    }
    
    /**
     * @return the next object node that is above the ReferenceToObjectInstance
     */
    public static String getParentObjectOfReference(ReadableModel model, String referencePath)
    {
        //iterate over the JSON and return the path of the next json node that is an object
        String parentPath = PathHelper.getParentPath(referencePath);
        if (parentPath == null)
        {
            return null;
        }
        // check if the node at the parent is an object
        JsonNodeWithPath parent = model.getNodeForPath(parentPath);
        if (parent != null && parent.getNode().isObject())
        {
            return parent.getPath();
        }
        return getParentObjectOfReference(model, parentPath);
    }
    
    /**
     * @return all References of the node at the path or its direct children
     */
    public static List<ReferenceToObjectInstance> findOutgoingReferences(String path, ReadableModel model)
    {
        List<ReferenceToObjectInstance> outgoingReferences = new ArrayList<>();
        
        // Check if the node itself is a ReferenceToObject
        ReferenceToObject referenceToObject = model.getReferenceToObject(path);
        if (referenceToObject != null)
        {
            outgoingReferences.addAll(getReferenceInstancesAtPath(model, referenceToObject, path));
        }
        
        JsonNodeWithPath nodeWithPath = model.getNodeForPath(path);
        JsonNode node = nodeWithPath.getNode();
        
        if (node.isObject())
        {
            node.fields().forEachRemaining(entry ->
            {
                // check if the child is a ReferenceToObjectInstance
                String childPath = path + "/" + entry.getKey();
                ReferenceToObject childReference = model.getReferenceToObject(childPath);
                if (childReference != null)
                {
                    //check if the child is an array
                    if (entry.getValue().isArray())
                    {
                        for (int index = 0; index < entry.getValue().size(); index++)
                        {
                            String itemPath = childPath + "/" + index;
                            outgoingReferences.addAll(getReferenceInstancesAtPath(model, childReference, itemPath));
                        }
                    }
                    else
                    {
                        outgoingReferences.addAll(getReferenceInstancesAtPath(model, childReference, childPath));
                    }
                }
            });
        }
        
        return outgoingReferences;
    }
    
    
    /**
     * returns the ReferenceToObject objects that are saved in our json schema
     */
    public static List<ReferenceToObject> getReferenceToObjectNodes(ReadableModel model)
    {
        List<ReferenceToObject> referenceToObjects = new ArrayList<>();
        JsonNode rootSchema = model.getRootSchema().getSchemaNode();
        if (rootSchema != null)
        {
            JsonNode refsArray = rootSchema.get("referencesToObjects");
            if (refsArray != null && refsArray.isArray())
            {
                for (JsonNode refNode : refsArray)
                {
                    if (refNode.isObject())
                    {
                        JsonNode pathNode = refNode.get("path");
                        JsonNode objectReferencingKeyNode = refNode.get("objectReferencingKey");
                        JsonNode objectKeyNode = refNode.get("objectKey");
                        JsonNode remarksNode = refNode.get("referenceRemarks");
                        if (pathNode != null && pathNode.isTextual() && objectKeyNode != null && objectKeyNode.isTextual()
                                    && objectReferencingKeyNode != null && objectReferencingKeyNode.isTextual() && remarksNode != null
                                    && remarksNode.isTextual())
                        {
                            referenceToObjects.add(
                                    new ReferenceToObject(pathNode.asText(), objectReferencingKeyNode.asText(), objectKeyNode.asText(),
                                            remarksNode.asText()));
                        }
                    }
                }
            }
        }
        return referenceToObjects;
    }
    
    /**
     * put in a path of a node, get a list of all paths that reference this node if its a ReferenceableObject
     */
    public static List<ReferenceToObjectInstance> getReferencesOfObject(ReadableModel model, String pathToReferenceableObject)
    {
        List<ReferenceToObjectInstance> refs = new ArrayList<>();
        if (model == null || pathToReferenceableObject == null)
        {
            return refs;
        }
        JsonNode node = model.getNodeForPath(pathToReferenceableObject).getNode();
        if (node == null)
        {
            return refs;
        }
        // first we find out if the node is a Referenceable Object
        // TODO maybe add an array exemption here
        ReferenceableObject referenceableObject = getReferenceableObjectOfPath(model, pathToReferenceableObject);
        if (referenceableObject == null)
        {
            return refs;
        }
        ReferenceableObjectInstance referenceableObjectInstance = new ReferenceableObjectInstance(model, referenceableObject,
                model.getNodeForPath(pathToReferenceableObject));
        // then we find out if there are any ReferenceToObjects that reference this object
        // we do this by iterating over all possible ReferenceToObject locations until we
        for (ReferenceToObject referenceToObject : getReferenceToObjectNodes(model))
        {
            for (ReferenceToObjectInstance instance : getInstancesOfReferenceToObject(model, referenceToObject))
            {
                if (instance.refersToObject(referenceableObjectInstance))
                {
                    refs.add(instance);
                }
            }
        }
        return refs;
    }
    
    public static void setObjectKeyOfInstance(ReadableModel model, ReferenceToObject reference, String pathToInstance, String newKey)
    {
        if (reference == null || newKey == null || newKey.isEmpty() || pathToInstance == null || pathToInstance.isEmpty())
        {
            return;
        }
        setKeyNode(model, pathToInstance, newKey, reference.getObjectKey());
        
    }
    
    /**
     * sets the key node of a node, assuming the node is an instance of a Refererenceable Object
     */
    public static void setKeyOfInstance(ReadableModel model, ReferenceableObject object, String pathToInstance, String newKey)
    {
        if (pathToInstance == null || newKey == null || newKey.isEmpty())
        {
            return;
        }
        
        String pathToKey = object.getKey();
        
        setKeyNode(model, pathToInstance, newKey, pathToKey);
    }
    
    private static void setKeyNode(ReadableModel model, String pathToNode, String newKey, String pathToKey)
    {
        String[] keyParts = pathToKey.split("/");
        JsonNode currentNode = model.getNodeForPath(pathToNode).getNode();
        JsonNode parentNode = null;
        
        for (String keyPart : keyParts)
        {
            if (keyPart.isEmpty())
            {
                continue;
            }
            parentNode = currentNode;
            currentNode = currentNode.path(keyPart);
            if (currentNode.isMissingNode())
            {
                return;
            }
        }
        
        if (parentNode instanceof ObjectNode)
        {
            // check if we need a text or number node
            List<String> types = SchemaHelper.getTypes(model.getSubschemaForPath(pathToNode + "/" + pathToKey).getSchemaNode());
            if (types.contains("string"))
            {
                ((ObjectNode) parentNode).set(keyParts[keyParts.length - 1], new TextNode(newKey));
            }
            else if (types.contains("integer") || types.contains("number"))
            {
                try
                {
                    ((ObjectNode) parentNode).set(keyParts[keyParts.length - 1], new IntNode(Integer.parseInt(newKey)));
                }
                catch (NumberFormatException e)
                {
                    logger.error("Could not parse key to number: " + newKey);
                }
            }
            
        }
    }
    
    public static List<ReferenceToObjectInstance> getInstancesOfReferenceToObject(ReadableModel model, ReferenceToObject object)
    {
        List<ReferenceToObjectInstance> instances = new ArrayList<>();
        if (object == null)
        {
            return instances;
        }
        // first we figure out which Json Nodes are the "nodes" of the reference
        // if the path of the reference contains a * then we need to take care. Otherwise we can just use the path as it is
        if (!object.getPath().contains("*"))
        {
            instances.addAll(getReferenceInstancesAtPath(model, object, object.getPath()));
        }
        else
        {
            // we have to resolve the path into the actual paths it entails
            for (String path : PathHelper.resolvePathWithWildcard(model, object.getPath()))
            {
                instances.addAll(getReferenceInstancesAtPath(model, object, path));
            }
        }
        return instances;
    }
    
    private static List<ReferenceToObjectInstance> getReferenceInstancesAtPath(ReadableModel model, ReferenceToObject object, String path)
    {
        List<ReferenceToObjectInstance> instances = new ArrayList<>();
        JsonNodeWithPath node = model.getNodeForPath(path);
        if (node == null)
        {
            return instances;
        }
        if (node.isArray())
        {
            // if the node is an array, we need to iterate over all items, because they are the references
            for (int index = 0; index < node.getNode().size(); index++)
            {
                String itemPath = node.getPath() + "/" + index;
                instances.add(new ReferenceToObjectInstance(model, object, model.getNodeForPath(itemPath)));
            }
        }
        else
        {
            // if the node is not an array, we just add it
            instances.add(new ReferenceToObjectInstance(model, object, node));
        }
        return instances;
        
    }
}
