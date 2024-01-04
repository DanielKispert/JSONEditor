package com.daniel.jsoneditor.model.json.schema.paths;

import java.util.Arrays;


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
    
    private static String[] formatPathToCheck(String pathToCheck)
    {
        String[] pathToCheckParts = pathToCheck.split("/");
        if (isInteger(pathToCheckParts[pathToCheckParts.length - 1]))
        {
            pathToCheckParts = Arrays.copyOf(pathToCheckParts, pathToCheckParts.length - 1);
        }
        return pathToCheckParts;
    }
    
    private static boolean isInteger(String str)
    {
        return str.matches("^\\d+$");
    }
}
