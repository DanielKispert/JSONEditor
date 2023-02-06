package jsoneditor.model.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public final class JsonNodeWithPath
{
    private final JsonNode node;
    private final String name;
    private final String path;
    
    public JsonNodeWithPath(JsonNode node, String path)
    {
        this.node = node;
        this.path = path;
        if (path.isEmpty())
        {
            this.name = "Root Element";
        }
        else
        {
            String[] pathSplit = path.split("/");
            String name = pathSplit[pathSplit.length - 1];
            if (node.isArray())
            {
                name += "[]";
            }
            this.name = name;
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonNodeWithPath that = (JsonNodeWithPath) o;
        return Objects.equals(getPath(), that.getPath()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getNode(), that.getNode());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getPath(), getDisplayName(), getNode());
    }
    
    public JsonNode getNode()
    {
        return node;
    }
    
    public String getDisplayName()
    {
        return name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    @Override
    public String toString()
    {
        return getDisplayName();
    }
    
}
