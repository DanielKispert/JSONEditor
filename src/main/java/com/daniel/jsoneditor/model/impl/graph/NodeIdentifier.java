package com.daniel.jsoneditor.model.impl.graph;

public class NodeIdentifier
{
    private boolean outgoingReferencesAdded = false;
    
    private final String path;
    
    public NodeIdentifier(String path)
    {
        this.path = path;
    }
    
    public boolean isOutgoingReferencesAdded()
    {
        return outgoingReferencesAdded;
    }
    
    public String getPath()
    {
        return path;
    }
}
