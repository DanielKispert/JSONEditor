package com.daniel.jsoneditor.model.json.schema.reference;

public class ReferenceToObject
{
    private final String objectReferencingKey;
    
    private final String objectKey;
    
    public ReferenceToObject(String objectReferencingKey, String objectKey)
    {
        this.objectReferencingKey = objectReferencingKey;
        this.objectKey = objectKey;
    }
    
    public String getObjectReferencingKey()
    {
        return objectReferencingKey;
    }
    
    public String getObjectKey()
    {
        return objectKey;
    }
}
