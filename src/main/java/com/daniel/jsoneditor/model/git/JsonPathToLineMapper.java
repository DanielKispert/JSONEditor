package com.daniel.jsoneditor.model.git;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps JSON paths to line numbers by parsing the JSON file with Jackson's streaming API.
 * Builds a complete path-to-line mapping during initialization for fast lookups.
 */
public class JsonPathToLineMapper
{
    private static final Logger logger = LoggerFactory.getLogger(JsonPathToLineMapper.class);
    
    private final Map<String, Integer> pathToLineMap = new HashMap<>();
    private final JsonFactory jsonFactory = new JsonFactory();
    
    /**
     * Parse JSON file and build complete path-to-line mapping.
     */
    public void buildMapping(Path jsonFilePath)
    {
        pathToLineMap.clear();
        
        try (JsonParser parser = jsonFactory.createParser(jsonFilePath.toFile()))
        {
            final Deque<String> pathStack = new ArrayDeque<>();
            final Deque<Integer> arrayIndexStack = new ArrayDeque<>();
            String currentFieldName = null;
            
            while (parser.nextToken() != null)
            {
                final JsonToken token = parser.currentToken();
                final int lineNumber = parser.getTokenLocation().getLineNr() - 1;
                
                switch (token)
                {
                    case FIELD_NAME:
                        currentFieldName = parser.currentName();
                        break;
                        
                    case START_OBJECT:
                        if (currentFieldName != null)
                        {
                            pathStack.push(currentFieldName);
                            final String path = buildPath(pathStack);
                            pathToLineMap.put(path, lineNumber);
                            currentFieldName = null;
                        }
                        else if (!pathStack.isEmpty() && isArrayContext(arrayIndexStack))
                        {
                            final int arrayIndex = arrayIndexStack.pop();
                            pathStack.push(String.valueOf(arrayIndex));
                            final String path = buildPath(pathStack);
                            pathToLineMap.put(path, lineNumber);
                            arrayIndexStack.push(arrayIndex + 1);
                        }
                        arrayIndexStack.push(0);
                        break;
                        
                    case END_OBJECT:
                        if (!pathStack.isEmpty())
                        {
                            pathStack.pop();
                        }
                        if (!arrayIndexStack.isEmpty())
                        {
                            arrayIndexStack.pop();
                        }
                        break;
                        
                    case START_ARRAY:
                        if (currentFieldName != null)
                        {
                            pathStack.push(currentFieldName);
                            final String path = buildPath(pathStack);
                            pathToLineMap.put(path, lineNumber);
                            currentFieldName = null;
                        }
                        arrayIndexStack.push(0);
                        break;
                        
                    case END_ARRAY:
                        if (!pathStack.isEmpty())
                        {
                            pathStack.pop();
                        }
                        if (!arrayIndexStack.isEmpty())
                        {
                            arrayIndexStack.pop();
                        }
                        break;
                        
                    default:
                        if (currentFieldName != null)
                        {
                            pathStack.push(currentFieldName);
                            final String path = buildPath(pathStack);
                            pathToLineMap.put(path, lineNumber);
                            pathStack.pop();
                            currentFieldName = null;
                        }
                        else if (!pathStack.isEmpty() && isArrayContext(arrayIndexStack))
                        {
                            final int arrayIndex = arrayIndexStack.pop();
                            pathStack.push(String.valueOf(arrayIndex));
                            final String path = buildPath(pathStack);
                            pathToLineMap.put(path, lineNumber);
                            pathStack.pop();
                            arrayIndexStack.push(arrayIndex + 1);
                        }
                        break;
                }
            }
            
            logger.debug("Built path-to-line mapping with {} entries", pathToLineMap.size());
        }
        catch (IOException e)
        {
            logger.error("Failed to parse JSON file: {}", jsonFilePath, e);
        }
    }
    
    /**
     * Get line number for a JSON path, or search for parent paths if not found.
     *
     * @param fullPath JSON path like "/root/child/property"
     * @return 0-based line number or -1 if not found
     */
    public int getLineForPathOrParent(String fullPath)
    {
        if (fullPath == null || fullPath.isEmpty())
        {
            return 0;
        }
        
        String searchPath = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;
        
        if (pathToLineMap.containsKey(searchPath))
        {
            return pathToLineMap.get(searchPath);
        }
        
        while (searchPath.contains("/"))
        {
            final int lastSlash = searchPath.lastIndexOf('/');
            searchPath = searchPath.substring(0, lastSlash);
            
            if (pathToLineMap.containsKey(searchPath))
            {
                return pathToLineMap.get(searchPath);
            }
        }
        
        return pathToLineMap.getOrDefault(searchPath, -1);
    }
    
    private String buildPath(Deque<String> pathStack)
    {
        if (pathStack.isEmpty())
        {
            return "";
        }
        
        final StringBuilder sb = new StringBuilder();
        final Object[] pathParts = pathStack.toArray();
        
        for (int i = pathParts.length - 1; i >= 0; i--)
        {
            sb.append(pathParts[i]);
            if (i > 0)
            {
                sb.append('/');
            }
        }
        
        return sb.toString();
    }
    
    private boolean isArrayContext(Deque<Integer> arrayIndexStack)
    {
        return !arrayIndexStack.isEmpty() && arrayIndexStack.peek() >= 0;
    }
}
