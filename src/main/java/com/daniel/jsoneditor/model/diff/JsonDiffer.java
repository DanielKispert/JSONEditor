package com.daniel.jsoneditor.model.diff;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class JsonDiffer
{
    private static final Logger logger = LoggerFactory.getLogger(JsonDiffer.class);
    
    private JsonDiffer()
    {
    }
    
    /**
     * Calculates differences between two JSON trees.
     *
     * @param savedJson JSON from file on disk
     * @param currentJson JSON currently in editor
     * @param model The model to check for references and referenceable objects
     * @return List of differences, never null
     */
    public static List<DiffEntry> calculateDiff(JsonNode savedJson, JsonNode currentJson, ReadableModel model)
    {
        final List<DiffEntry> diffs = new ArrayList<>();
        compareNodes("", savedJson, currentJson, diffs, model);
        logger.debug("Found {} differences", diffs.size());
        return diffs;
    }
    
    private static void compareNodes(String path, JsonNode saved, JsonNode current, List<DiffEntry> diffs,
                                     ReadableModel model)
    {
        if (saved == null || saved.isNull())
        {
            if (current != null && !current.isNull())
            {
                final DiffEntry.EntryType entryType = determineEntryType(path, model);
                diffs.add(new DiffEntry(path.isEmpty() ? "/" : path, DiffEntry.DiffType.ADDED, null, current, entryType));
            }
            return;
        }
        
        if (current == null || current.isNull())
        {
            final DiffEntry.EntryType entryType = determineEntryType(path, model);
            diffs.add(new DiffEntry(path.isEmpty() ? "/" : path, DiffEntry.DiffType.REMOVED, saved, null, entryType));
            return;
        }
        
        if (saved.getNodeType() != current.getNodeType())
        {
            final DiffEntry.EntryType entryType = determineEntryType(path, model);
            diffs.add(new DiffEntry(path.isEmpty() ? "/" : path, DiffEntry.DiffType.MODIFIED, saved, current, entryType));
            return;
        }
        
        if (saved.isObject())
        {
            compareObjects(path, saved, current, diffs, model);
        }
        else if (saved.isArray())
        {
            compareArrays(path, saved, current, diffs, model);
        }
        else if (!saved.equals(current))
        {
            final DiffEntry.EntryType entryType = determineEntryType(path, model);
            diffs.add(new DiffEntry(path.isEmpty() ? "/" : path, DiffEntry.DiffType.MODIFIED, saved, current, entryType));
        }
    }
    
    private static DiffEntry.EntryType determineEntryType(String path, ReadableModel model)
    {
        if (model.getReferenceToObject(path) != null)
        {
            return DiffEntry.EntryType.REFERENCE_TO_OBJECT;
        }
        if (model.getReferenceableObject(path) != null)
        {
            return DiffEntry.EntryType.REFERENCEABLE_OBJECT;
        }
        return DiffEntry.EntryType.NORMAL;
    }
    
    private static void compareObjects(String path, JsonNode saved, JsonNode current, List<DiffEntry> diffs,
                                       ReadableModel model)
    {
        final Iterator<Map.Entry<String, JsonNode>> savedFields = saved.fields();
        
        while (savedFields.hasNext())
        {
            final Map.Entry<String, JsonNode> entry = savedFields.next();
            final String fieldName = entry.getKey();
            final String fieldPath = path.isEmpty() ? "/" + fieldName : path + "/" + fieldName;
            
            final JsonNode savedChild = entry.getValue();
            final JsonNode currentChild = current.get(fieldName);
            
            compareNodes(fieldPath, savedChild, currentChild, diffs, model);
        }
        
        final Iterator<String> currentFieldNames = current.fieldNames();
        while (currentFieldNames.hasNext())
        {
            final String fieldName = currentFieldNames.next();
            if (!saved.has(fieldName))
            {
                final String fieldPath = path.isEmpty() ? "/" + fieldName : path + "/" + fieldName;
                compareNodes(fieldPath, null, current.get(fieldName), diffs, model);
            }
        }
    }
    
    private static void compareArrays(String path, JsonNode saved, JsonNode current, List<DiffEntry> diffs,
                                      ReadableModel model)
    {
        final int savedSize = saved.size();
        final int currentSize = current.size();
        final int maxSize = Math.max(savedSize, currentSize);
        
        for (int i = 0; i < maxSize; i++)
        {
            final String itemPath = path + "/" + i;
            final JsonNode savedItem = i < savedSize ? saved.get(i) : null;
            final JsonNode currentItem = i < currentSize ? current.get(i) : null;
            
            compareNodes(itemPath, savedItem, currentItem, diffs, model);
        }
    }
}

