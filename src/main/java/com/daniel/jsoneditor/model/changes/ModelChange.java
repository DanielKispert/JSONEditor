package com.daniel.jsoneditor.model.changes;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Immutable description of a semantic model mutation used for UI updates and undo/redo.
 * Instances are created via static factory methods for clarity of intent.
 */
public final class ModelChange
{
    private final ChangeType type;
    
    private final String path;
    
    private final JsonNode oldValue;
    
    private final JsonNode newValue;
    
    private final Integer fromIndex;
    
    private final Integer toIndex;
    
    private ModelChange(ChangeType type, String path, JsonNode oldValue, JsonNode newValue, Integer fromIndex, Integer toIndex)
    {
        this.type = type;
        this.path = path;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }
    
    public static ModelChange add(String path, JsonNode newValue)
    {
        return new ModelChange(ChangeType.ADD, path, null, newValue, null, null);
    }
    
    public static ModelChange remove(String path, JsonNode oldValue)
    {
        return new ModelChange(ChangeType.REMOVE, path, oldValue, null, null, null);
    }
    
    public static ModelChange replace(String path, JsonNode oldValue, JsonNode newValue)
    {
        return new ModelChange(ChangeType.REPLACE, path, oldValue, newValue, null, null);
    }
    
    public static ModelChange move(String path, Integer fromIndex, Integer toIndex)
    {
        return new ModelChange(ChangeType.MOVE, path, null, null, fromIndex, toIndex);
    }
    
    public static ModelChange settingsChanged(JsonNode oldSettings, JsonNode newSettings)
    {
        return new ModelChange(ChangeType.SETTINGS_CHANGED, "__settings__", oldSettings, newSettings, null, null);
    }
    
    public static ModelChange sort(String path, JsonNode oldArraySnapshot, JsonNode newArraySnapshot)
    {
        return new ModelChange(ChangeType.SORT, path, oldArraySnapshot, newArraySnapshot, null, null);
    }
    
    public ChangeType getType()
    {
        return type;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public JsonNode getOldValue()
    {
        return oldValue;
    }
    
    public JsonNode getNewValue()
    {
        return newValue;
    }
    
    public Integer getFromIndex()
    {
        return fromIndex;
    }
    
    public Integer getToIndex()
    {
        return toIndex;
    }
    
    @Override
    public String toString()
    {
        return "ModelChange{" + "type=" + type + ", path='" + path + '\'' + ", oldValue=" + oldValue + ", newValue=" + newValue
                + ", fromIndex=" + fromIndex + ", toIndex=" + toIndex + '}';
    }
}
