package com.daniel.jsoneditor.model.json.schema.reference;

import com.fasterxml.jackson.databind.JsonNode;


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
}
