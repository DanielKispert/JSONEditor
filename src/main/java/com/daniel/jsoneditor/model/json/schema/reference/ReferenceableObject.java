package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;


public class ReferenceableObject
{
    private final String referencingKey;
    
    private final String path;
    
    private final String key;
    
    public ReferenceableObject(String referencingKey, String path, String key)
    {
        this.referencingKey = referencingKey;
        this.path = path;
        this.key = key;
    }
    
    public ReferenceableObject(String referencingKey, String path)
    {
        this(referencingKey, path, null);
    }
    
    public String getReferencingKey()
    {
        return referencingKey;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public String getKeyOfInstance(JsonNode node)
    {
        if (node != null)
        {
            JsonNode keyNode = node.at(key);
            if (keyNode != null && !keyNode.isMissingNode())
            {
                return keyNode.asText();
            }
        }
        
        return null;
    }
    
    /**
     * @return first part of the pair is the key (that identifies this referenceable object), the second part is info for the UI
     */
    public Pair<String, String> getDetailsOfInstance(ReadableModel model, JsonNodeWithPath nodeWithPath)
    {
        String key = getKeyOfInstance(nodeWithPath.getNode());
        if (key != null)
        {
            return new Pair<>(key, nodeWithPath.makeNameIncludingPath(model));
        }
        return null;
    }
}
