package com.daniel.jsoneditor.model.json.schema.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;


public class ReferenceHelper
{
    public static String resolveReference(JsonNodeWithPath node, ReferenceToObject reference, ReadableModel model)
    {
        if (node == null || reference == null || model == null)
        {
            return null;
        }
        String objectReferencingKey = node.getNode().at(reference.getObjectReferencingKey()).asText();
        String id = node.getNode().at(reference.getObjectKey()).asText();
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
                        if (id.equals(object.getKeyOfInstance(item)))
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
        System.out.println(
                "Could not find a referenceable object for reference " + node.getDisplayName());
        return null;
    }
    
    public static List<Pair<String, String>> getKeyOfReferenceableObjectInstances(ReadableModel model, ReferenceableObject referenceableObject)
    {
        JsonNodeWithPath objectInstance = model.getNodeForPath(referenceableObject.getPath());
        if (objectInstance.isArray())
        {
            List<Pair<String, String>> keys = new ArrayList<>();
            JsonNode arrayNode = objectInstance.getNode();
            for (int index = 0; index < arrayNode.size(); index++)
            {
                String itemPath = objectInstance.getPath() + "/" + index;
                
                // the array items are the referenceable objects
                Pair<String, String> details = referenceableObject.getDetailsOfInstance(model, model.getNodeForPath(itemPath));
                if (details != null)
                {
                    keys.add(details);
                }
            }
            return keys;
        }
        else
        {
            // the referenceable object is the object itself, so we get its key
            return Collections.singletonList(referenceableObject.getDetailsOfInstance(model, objectInstance));
        }
        
    }
}
