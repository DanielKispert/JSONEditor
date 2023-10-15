package com.daniel.jsoneditor.model.json.schema.reference;

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
}
