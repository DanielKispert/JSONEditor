package com.daniel.jsoneditor.model.impl.graph;

import java.util.Objects;


public class VertexIdentifier
{
    private final String path;
    
    private final String name;
    
    public VertexIdentifier(String path, String name)
    {
        this.path = path;
        this.name = name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getName()
    {
        return name;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        VertexIdentifier that = (VertexIdentifier) o;
        return Objects.equals(path, that.path);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(path);
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
