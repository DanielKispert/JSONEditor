package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;


public class ReferenceableObjectInstance
{
    private final String key;
    
    private final String path;
    
    private final String fancyName;
    
    public ReferenceableObjectInstance(ReadableModel model, ReferenceableObject object, JsonNodeWithPath node)
    {
        this.path = node.getPath();
        this.fancyName = node.makeNameIncludingPath(model);
        this.key = object.getKeyOfInstance(node.getNode());
    }
    
    public String getKey()
    {
        return key;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getFancyName()
    {
        return fancyName;
    }
}
