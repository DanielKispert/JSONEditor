package com.daniel.jsoneditor.model.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Simple mapper that finds line numbers for JSON property keys.
 * For MVP: searches for the last path segment as a JSON key.
 */
public class JsonPathToLineMapper
{
    private static final Logger logger = LoggerFactory.getLogger(JsonPathToLineMapper.class);
    
    private List<String> lines;
    
    public void buildMapping(Path jsonFilePath)
    {
        try
        {
            lines = Files.readAllLines(jsonFilePath);
        }
        catch (IOException e)
        {
            logger.error("Failed to read JSON file: {}", jsonFilePath, e);
            lines = null;
        }
    }
    
    /**
     * Find line number for a JSON path by searching for the last path segment.
     *
     * @param fullPath JSON path like "/root/child/property"
     * @return 0-based line number or -1 if not found
     */
    public int getLineForPathOrParent(String fullPath)
    {
        if (lines == null || fullPath == null || fullPath.isEmpty())
        {
            return -1;
        }
        
        final String lastSegment = getLastSegment(fullPath);
        if (lastSegment == null)
        {
            return 0;
        }
        
        final String searchPattern = "\"" + lastSegment + "\"";
        
        for (int i = 0; i < lines.size(); i++)
        {
            if (lines.get(i).contains(searchPattern))
            {
                return i;
            }
        }
        
        return -1;
    }
    
    private String getLastSegment(String path)
    {
        if (path == null || path.isEmpty())
        {
            return null;
        }
        
        final int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == path.length() - 1)
        {
            return null;
        }
        
        return path.substring(lastSlash + 1);
    }
}
