package com.daniel.jsoneditor.model.impl.graph;

import java.util.HashMap;
import java.util.Map;


public class NodeIdentifierCache
{
    
    private static final Map<String, NodeIdentifier> nodeIdentifiers = new HashMap<>();
    
    public static NodeIdentifier get(String path)
    {
        if (nodeIdentifiers.containsKey(path))
        {
            return nodeIdentifiers.get(path);
        }
        NodeIdentifier nodeIdentifier = new NodeIdentifier(path);
        nodeIdentifiers.put(path, nodeIdentifier);
        return nodeIdentifier;
    }
    
    
}
