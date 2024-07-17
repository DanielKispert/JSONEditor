package com.daniel.jsoneditor.model.impl.graph;

public class NodeIdentifier
{
    private boolean outgoingReferencesAdded = false;
    
    private double layer; //number between 0 and 1 that sets what level of the window the node gravitates towards. 0 is the top, 1 is the bottom
    
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
    
    public double getLayer()
    {
        return layer;
    }
    
    public void setLayer(double layer)
    {
        this.layer = layer;
    }
}
