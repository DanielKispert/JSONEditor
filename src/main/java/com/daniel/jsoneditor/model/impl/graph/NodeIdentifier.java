package com.daniel.jsoneditor.model.impl.graph;

import java.util.List;


public class NodeIdentifier
{
    private boolean outgoingReferencesAdded = false;
    
    private boolean isCluster;
    
    private List<String> clusterPaths;
    
    private double layer; //number between 0 and 1 that sets what level of the window the node gravitates towards. 0 is the top, 1 is the bottom
    
    private final String path;
    
    public NodeIdentifier(String path)
    {
        this.path = path;
        this.layer = 0.5;
        isCluster = false;
    }
    
    public boolean isCluster()
    {
        return isCluster;
    }
    
    public void setCluster(List<String> paths)
    {
        this.isCluster = true;
        this.clusterPaths = paths;
    }
    
    public List<String> getClusterPaths()
    {
        return clusterPaths;
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
    
    @Override
    public String toString()
    {
        return "NodeIdentifier{path='" + path + '\'' +'}';
    }
}
