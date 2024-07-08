package com.daniel.jsoneditor.model.json.schema.reference;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;


/**
 * a node in a json file that at the same time represents a ReferenceToObject
 */
public class ReferenceToObjectInstance implements ReferencingInstance
{
    private final String key;
    
    private final String path;
    
    private final String fancyName;
    
    private final String referencingKey;
    
    private final ReferenceToObject reference;
    
    private final String remarks;
    
    public ReferenceToObjectInstance(ReadableModel model, ReferenceToObject object, JsonNodeWithPath node)
    {
        this.path = node.getPath();
        this.reference = object;
        this.fancyName = node.makeNameIncludingPath(model);
        this.key = reference.getKeyOfInstance(node.getNode());
        this.referencingKey = node.getNode().at(reference.getObjectReferencingKey()).asText();
        this.remarks = reference.getRemarksOfInstance(node.getNode());
    }
    
    @Override
    public String getKey()
    {
        return key;
    }
    
    public ReferenceToObject getReference()
    {
        return reference;
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
    
    public String getRemarks()
    {
        return remarks;
    }
    
    public boolean refersToObject(ReferenceableObjectInstance objectInstance)
    {
        return objectInstance.getKey().equals(key) && objectInstance.getReferencingKey().equals(referencingKey);
    }
}
