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
            if (node.size() == 0)
            {
                return "{}";
            }
            final StringBuilder sb = new StringBuilder("{");
            int count = 0;
            final int maxFields = 3;
            final var fields = node.fields();
            while (fields.hasNext() && count < maxFields)
            {
                final var entry = fields.next();
                if (count > 0)
                {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append(": ");
                final String fieldValue = formatFieldPreview(entry.getValue());
                sb.append(fieldValue);
                count++;
            }
            if (node.size() > maxFields)
            {
                sb.append(", ...");
            }
            sb.append("}");
            final String result = sb.toString();
            return result.length() > 120 ? result.substring(0, 117) + "...}" : result;
        }
        if (node.isArray())
        {
            if (node.size() == 0)
            {
                return "[]";
            }
            final StringBuilder sb = new StringBuilder("[");
            final int maxItems = 3;
            for (int i = 0; i < Math.min(node.size(), maxItems); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append(formatFieldPreview(node.get(i)));
            }
            if (node.size() > maxItems)
            {
                sb.append(", ...");
            }
            sb.append("]");
            final String result = sb.toString();
            return result.length() > 120 ? result.substring(0, 117) + "...]" : result;
        }
        return node.toString();
    }
    
    private String formatFieldPreview(JsonNode value)
    {
        if (value == null || value.isNull())
        {
            return "null";
        }
        if (value.isTextual())
        {
            final String text = value.asText();
            if (text.length() > 30)
            {
                return "\"" + text.substring(0, 27) + "...\"";
            }
            return "\"" + text + "\"";
        }
        if (value.isNumber() || value.isBoolean())
        {
            return value.asText();
        }
        if (value.isObject())
        {
            return "{" + value.size() + "}";
        }
        if (value.isArray())
        {
            return "[" + value.size() + "]";
        }
        return value.toString();
    }
}

