package jsoneditor.model.json;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeWithPath
{
    private final String path;
    
    private final String name;
    
    private final JsonNode node;
    
    public JsonNodeWithPath(JsonNode node, String name, String path)
    {
        this.path = path;
        this.name = name;
        this.node = node;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getName()
    {
        return name;
    }
    
    public JsonNode getNode()
    {
        return node;
    }
}
