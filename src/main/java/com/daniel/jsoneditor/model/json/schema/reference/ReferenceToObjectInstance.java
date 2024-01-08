package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;


/**
 * a node in a json file that at the same time represents a ReferenceToObject
 */
public class ReferenceToObjectInstance implements HasKeyAndFancyName
{
    private final String key;
    
    private final String path;
    
    private final String fancyName;
    
    public ReferenceToObjectInstance(ReadableModel model, ReferenceToObject object, JsonNodeWithPath node)
    {
        this.path = node.getPath();
        this.fancyName = node.makeNameIncludingPath(model);
        this.key = object.getKeyOfInstance(node.getNode());
    }
    
    @Override
    public String getKey()
    {
        return key;
    }
    
    @Override
    public String getPath()
    {
        return path;
    }
    
    @Override
    public String getFancyName()
    {
        return fancyName;
    }
}
