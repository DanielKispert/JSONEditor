package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;

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
                        if (id.equals(item.at(object.getKey()).asText()))
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
}
