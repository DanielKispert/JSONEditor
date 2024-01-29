package com.daniel.jsoneditor.model.json;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
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
            else if (node.isObject() && isStringInt(name))
            {
                name = makeFancyObjectName();
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
    
    private String makeFancyObjectName()
    {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); )
        {
            Map.Entry<String, JsonNode> field = it.next();
            JsonNode value = field.getValue();
            if (!value.isArray() && !value.isObject())
            {
                if (index++ != 0)
                {
                    builder.append("|");
                }
                builder.append(value.asText());
            }
        }
        return builder.toString();
    }
    
    private boolean isStringInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
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
    
    public boolean isArray()
    {
        return node != null && node.isArray();
    }
    
    public boolean isObject()
    {
        return node != null && node.isObject();
    }
    
    public String makeNameIncludingPath(ReadableModel model)
    {
        String path = getPath();
        StringBuilder fancyName = new StringBuilder();
        int startIndex = 0;
        int nextIndex;
        // first we grab the first path bit, then the first and second, and so on
        while ((nextIndex = path.indexOf("/", startIndex)) != -1)
        {
            String partialPath = path.substring(0, nextIndex);
            JsonNodeWithPath pathNode = model.getNodeForPath(partialPath);
            String displayName = pathNode.getDisplayName();
            fancyName.append(displayName);
            fancyName.append(" > ");
            startIndex = nextIndex + 1;
        }
        
        // the last part of the path has to be handled separately
        JsonNodeWithPath lastPathNode = model.getNodeForPath(path);
        fancyName.append(lastPathNode.getDisplayName());
        
        return fancyName.toString();
    }
    
    public void removeProperty(String propertyName)
    {
        if (node != null && node.isObject())
        {
            ObjectNode node = (ObjectNode) this.node;
            node.remove(propertyName);
        }
    }
    
    public void setProperty(String propertyName, Object value)
    {
        if (node != null && node.isObject())
        {
            ObjectNode node = (ObjectNode) this.node;
            // if the property name starts with / we remove that
            // TODO add proper path handling here for nested children
            if (propertyName.startsWith("/"))
            {
                propertyName = propertyName.substring(1);
            }
            if (value instanceof String)
            {
                node.put(propertyName, (String) value);
            }
            else if (value instanceof Integer)
            {
                node.put(propertyName, (Integer) value);
            }
            else if (value instanceof Double)
            {
                node.put(propertyName, (Double) value);
            }
        }
    }
    
}
