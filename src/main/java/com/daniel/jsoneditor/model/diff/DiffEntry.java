package com.daniel.jsoneditor.model.diff;

import com.fasterxml.jackson.databind.JsonNode;


public class DiffEntry
{
    public enum DiffType
    {
        ADDED, REMOVED, MODIFIED
    }
    
    /**
     * Type of entry based on schema metadata.
     */
    public enum EntryType
    {
        /** Normal JSON node without special schema metadata */
        NORMAL,
        /** Entity reference (e.g., /processes/0/entityReferences/3) */
        REFERENCE_TO_OBJECT,
        /** Referenceable object instance (e.g., /processes/0, /fields/5) */
        REFERENCEABLE_OBJECT
    }
    
    private final String path;
    private final DiffType type;
    private final JsonNode oldValue;
    private final JsonNode newValue;
    private final EntryType entryType;
    
    public DiffEntry(String path, DiffType type, JsonNode oldValue, JsonNode newValue, EntryType entryType)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (type == null)
        {
            throw new IllegalArgumentException("DiffType cannot be null");
        }
        if (entryType == null)
        {
            throw new IllegalArgumentException("EntryType cannot be null");
        }
        
        this.path = path;
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.entryType = entryType;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public DiffType getType()
    {
        return type;
    }
    
    public JsonNode getOldValue()
    {
        return oldValue;
    }
    
    public JsonNode getNewValue()
    {
        return newValue;
    }
    
    public EntryType getEntryType()
    {
        return entryType;
    }
    
    public String getDisplayValue()
    {
        switch (type)
        {
            case ADDED:
                return formatValue(newValue);
            case REMOVED:
                return formatValue(oldValue);
            case MODIFIED:
                return formatValue(oldValue) + " → " + formatValue(newValue);
            default:
                return "";
        }
    }
    
    private String formatValue(JsonNode node)
    {
        if (node == null || node.isNull())
        {
            return "null";
        }
        if (node.isTextual())
        {
            final String text = node.asText();
            if (text.length() > 100)
            {
                return "\"" + text.substring(0, 97) + "...\"";
            }
            return "\"" + text + "\"";
        }
        if (node.isNumber() || node.isBoolean())
        {
            return node.asText();
        }
        if (node.isObject())
        {
            final int fieldCount = node.size();
            return fieldCount == 0 ? "{}" : "{" + fieldCount + " fields}";
        }
        if (node.isArray())
        {
            final int size = node.size();
            return size == 0 ? "[]" : "[" + size + " items]";
        }
        return node.toString();
    }
}

