package com.daniel.jsoneditor.model.json.schema.paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;


public class PathHelper
{
    public static boolean pathsMatch(String pathToCheck, String wildcardPath)
    {
        if (pathToCheck == null || wildcardPath == null)
        {
            return false;
        }
        String[] pathToCheckParts = formatPathToCheck(pathToCheck);
        String[] wildCardPathParts = wildcardPath.split("/");
        
        if (pathToCheckParts.length != wildCardPathParts.length)
        {
            return false;
        }
        
        for (int i = 0; i < pathToCheckParts.length; i++)
        {
            String pathPartToCheck = pathToCheckParts[i];
            String wildCardPathPart = wildCardPathParts[i];
            
            if (!wildCardPathPart.equals("*") && !wildCardPathPart.equals(pathPartToCheck))
            {
                return false;
            }
        }
        return true;
    }
    
    public static List<String> resolvePathWithWildcard(ReadableModel model, String pathWithWildcard)
    {
        // TODO we support only a single wildcard right now
        List<String> paths = new ArrayList<>();
        
        List<String> pathParts = Arrays.asList(pathWithWildcard.split("/\\*"));
        if (pathParts.size() != 2)
        {
            return paths;
        }
        for (JsonNodeWithPath child : getAllChildrenOfPath(model, pathParts.get(0)))
        {
            String pathWithoutWildcard = child.getPath() + pathParts.get(1);
            paths.add(pathWithoutWildcard);
        }
        
        return paths;
    }
    
    private static List<JsonNodeWithPath> getAllChildrenOfPath(ReadableModel model, String path)
    {
        List<JsonNodeWithPath> children = new ArrayList<>();
        JsonNode parent = model.getNodeForPath(path).getNode();
        
        if (parent != null)
        {
            if (parent.isObject())
            {
                parent.fieldNames().forEachRemaining(
                        fieldName -> children.add(new JsonNodeWithPath(parent.get(fieldName), path + "/" + fieldName)));
            }
            else if (parent.isArray())
            {
                IntStream.range(0, parent.size()).forEach(i -> children.add(new JsonNodeWithPath(parent.get(i), path + "/" + i)));
            }
        }
        return children;
    }
    
    private static String[] formatPathToCheck(String pathToCheck)
    {
        String[] pathToCheckParts = pathToCheck.split("/");
        // Check if the array is not empty and the last part is an integer
        if (pathToCheckParts.length > 0 && isInteger(pathToCheckParts[pathToCheckParts.length - 1]))
        {
            pathToCheckParts = Arrays.copyOf(pathToCheckParts, pathToCheckParts.length - 1);
        }
        return pathToCheckParts;
    }
    
    private static boolean isInteger(String str)
    {
        return str.matches("^\\d+$");
    }
    
    public static String getParentPath(String path)
    {
        if (path == null || path.isEmpty())
        {
            return null;
        }
        
        // Remove the trailing slash if it exists
        String trimmedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        
        // Find the last index of '/'
        int lastIndex = trimmedPath.lastIndexOf('/');
        if (lastIndex >= 0)
        {
            return trimmedPath.substring(0, lastIndex);
        }
        
        return null;
    }
    
    public static String getLastPathSegment(String path)
    {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] segments = path.split("/");
        if (segments.length == 0) {
            return null;
        }
        
        return segments[segments.length - 1];
    }
}
