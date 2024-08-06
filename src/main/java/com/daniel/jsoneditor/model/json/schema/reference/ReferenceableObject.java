package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;


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
