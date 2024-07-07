package com.daniel.jsoneditor.model.impl.graph;

/**
 * we can't use a string as an edge identifier, because we need different elements for different edges. These objects are used to differentiate the edges properly
 */
public class EdgeIdentifier
{
    private final String from;
    private final String to;
    private final String name;
    
    public EdgeIdentifier(String from, String to, String name)
    {
        this.from = from;
        this.to = to;
        this.name = name;
    }
    
    public String getFrom()
    {
        return from;
    }
    
    public String getTo()
    {
        return to;
    }
    
    public String getName()
    {
        return name;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeIdentifier that = (EdgeIdentifier) o;
        return from.equals(that.from) && to.equals(that.to) && name.equals(that.name);
    }
    
    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(from, to, name);
    }
    
    @Override
    public String toString()
    {
        return getName();
    }
}
