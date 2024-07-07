package com.daniel.jsoneditor.model.impl.graph;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class NodeGraphCreator
{
    /**
     * if the path contains a node, we build a graph with this as the "center". The graph is directed and contains all ReferenceToObjectInstances that point to this node, and also all references that go out from it
     */
    public static Digraph<String, EdgeIdentifier> createGraph(ReadableModel model, String path)
    {
        Digraph<String, EdgeIdentifier> graph = new DigraphEdgeList<>();
        graph.insertVertex(path);
        
        JsonNodeWithPath centralNodeWithPath = model.getNodeForPath(path);
        JsonNode centralNode = centralNodeWithPath.getNode();
        addOutgoingReferences(model, centralNode, path, graph);
        addIncomingReferences(model, path, graph);
        
        return graph;
    }
    
    private static void addOutgoingReferences(ReadableModel model, JsonNode node, String currentPath, Digraph<String, EdgeIdentifier> graph)
    {
        if (node.isObject())
        {
            node.fields().forEachRemaining(entry ->
            {
                String childPath = currentPath + "/" + entry.getKey();
                graph.insertVertex(childPath);
                graph.insertEdge(currentPath, childPath, new EdgeIdentifier(currentPath, childPath, entry.getKey()));
                JsonNode childNode = entry.getValue();
                addOutgoingReferences(model, childNode, childPath, graph);
            });
        }
        else if (node.isArray())
        {
            for (int i = 0; i < node.size(); i++)
            {
                String childPath = currentPath + "/" + i;
                graph.insertVertex(childPath);
                graph.insertEdge(currentPath, childPath, new EdgeIdentifier(currentPath, childPath, String.valueOf(i)));
                JsonNode childNode = node.get(i);
                addOutgoingReferences(model, childNode, childPath, graph);
            }
        }
    }
    
    private static void addIncomingReferences(ReadableModel model, String path, Digraph<String, EdgeIdentifier> graph)
    {
        List<ReferenceToObjectInstance> incomingReferences = ReferenceHelper.getReferencesOfObject(model, path);
        for (ReferenceToObjectInstance ref : incomingReferences)
        {
            String fromPath = ref.getPath();
            if (!graph.vertices().contains(fromPath))
            {
                graph.insertVertex(fromPath);
            }
            graph.insertEdge(fromPath, path, new EdgeIdentifier(fromPath, path, ref.getPath()));
        }
    }
    
}
