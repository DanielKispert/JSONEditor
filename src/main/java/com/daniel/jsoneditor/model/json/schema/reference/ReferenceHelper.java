package com.daniel.jsoneditor.model.json.schema.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;


public class ReferenceHelper
{
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
        for (ReferenceableObject object : model.getReferenceableObjects())
        {
            if (object.getReferencingKey().equals(objectReferencingKey))
            {
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
                }
                else
                {
                    return objectNode.getPath();
                }
                break;
            }
        }
        System.out.println("Could not find a referenceable object for reference " + node.getDisplayName());
        return null;
    }
    
    /**
     * goes through the currently loaded json and returns all nodes that are instances of this referenceable object
     */
    public static List<ReferenceableObjectInstance> getReferenceableObjectInstances(ReadableModel model,
            ReferenceableObject referenceableObject)
    {
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
     * returns the referenceable object that is
     */
    public static ReferenceableObject getReferenceableObjectOfPath(ReadableModel model, String path)
    {
        // we first check whether the object itself is the referenceableObject. But only if we're not an array. If we're an array we can't be a referenceable object (for reasons)
        JsonNodeWithPath node = model.getNodeForPath(path);
        List<ReferenceableObject> referenceableObjects = model.getReferenceableObjects();
        if (node != null && !node.isArray())
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
     * @return check all children of the given path, if its an object node, and return the ReferenceToObjectInstances that this object points to
     */
    public static List<ReferenceToObjectInstance> getReferencesBelowObject(ReadableModel model, String pathToObjectNode)
    {
        List<ReferenceToObjectInstance> references = new ArrayList<>();
        JsonNodeWithPath node = model.getNodeForPath(pathToObjectNode);
        
        if (node != null && node.getNode().isObject())
        {
            for (JsonNode field : node.getNode())
            {
                String fieldPath = node.getPath() + "/" + field.asText();
                ReferenceToObject referenceToObject = model.getReferenceToObject(fieldPath);
                if (referenceToObject != null)
                {
                    references.add(new ReferenceToObjectInstance(model, referenceToObject, model.getNodeForPath(fieldPath)));
                }
            }
        }
        
        return references;
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
                        if (pathNode != null && pathNode.isTextual() && objectKeyNode != null && objectKeyNode.isTextual()
                                && objectReferencingKeyNode != null && objectReferencingKeyNode.isTextual())
                        {
                            referenceToObjects.add(
                                    new ReferenceToObject(pathNode.asText(), objectReferencingKeyNode.asText(), objectKeyNode.asText()));
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
    
    private static List<ReferenceToObjectInstance> getInstancesOfReferenceToObject(ReadableModel model, ReferenceToObject object)
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
