package com.daniel.jsoneditor.model.json.schema.reference;

import com.fasterxml.jackson.databind.JsonNode;


public class ReferenceToObject
{
    private final String path;
    private final String objectReferencingKey;
    
    private final String objectKey;
    
    private final String remarks;
    
    public ReferenceToObject(String path, String objectReferencingKey, String objectKey, String remarks)
    {
        this.path = path;
        this.objectReferencingKey = objectReferencingKey;
        this.objectKey = objectKey;
        this.remarks = remarks;
    }
    
    public String getObjectReferencingKey()
    {
        return objectReferencingKey;
    }
    
    public String getObjectKey()
    {
        return objectKey;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getKeyOfInstance(JsonNode node)
    {
        if (node != null)
        {
            JsonNode keyNode = node.at(objectKey);
            if (keyNode != null && !keyNode.isMissingNode())
            {
                return keyNode.asText();
            }
        }
        
        return null;
    }
    
    public String getRemarksOfInstance(JsonNode node)
    {
        if (node != null)
        {
            JsonNode keyNode = node.at(remarks);
            if (keyNode != null && !keyNode.isMissingNode())
            {
                return keyNode.asText();
            }
        }
        
        return null;
    }
}
