package com.daniel.jsoneditor.model.impl.graph;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Predicate;


public class NodeGraphCreator
{
    /**
     * if the path contains a node, we build a graph with this as the "center". The graph is directed and contains all ReferenceToObjectInstances that point to this node, and also all references that go out from it
     */
    public static Digraph<String, EdgeIdentifier> createGraph(ReadableModel model, String path)
    {
        if (path == null)
        {
            return null;
        }
        Digraph<String, EdgeIdentifier> graph = new DigraphEdgeList<>();
        graph.insertVertex(path);
        addIncomingReferences(model, path, graph);
        addOutgoingReferences(model, path, graph);
        
        return graph;
    }
    
    private static void addOutgoingReferences(ReadableModel model, String currentPath, Digraph<String, EdgeIdentifier> graph)
    {
        List<ReferenceToObjectInstance> outgoingReferences = ReferenceHelper.findOutgoingReferences(currentPath, model);
        for (ReferenceToObjectInstance ref : outgoingReferences)
        {
            String toPath = ref.getPath();
            if (!graph.vertices().contains(toPath))
            {
                graph.insertVertex(toPath);
            }
            graph.insertEdge(currentPath, toPath, new EdgeIdentifier(currentPath, toPath, ref.getPath()));
        }
    }
    
    private static void addIncomingReferences(ReadableModel model, String path, Digraph<String, EdgeIdentifier> graph)
    {
        List<ReferenceToObjectInstance> incomingReferences = ReferenceHelper.getReferencesOfObject(model, path);
        for (ReferenceToObjectInstance ref : incomingReferences)
        {
            //the path is the parent object of that reference
            String fromPath = ReferenceHelper.getParentObjectOfReference(model, ref.getPath());
            
            if (graph.vertices().stream().noneMatch(stringVertex -> stringVertex.element().equals(fromPath)))
            {
                graph.insertVertex(fromPath);
            }
            graph.insertEdge(fromPath, path, new EdgeIdentifier(fromPath, path, ref.getPath()));
        }
    }
    
}
