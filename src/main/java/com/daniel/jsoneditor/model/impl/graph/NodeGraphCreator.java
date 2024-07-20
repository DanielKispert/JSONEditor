package com.daniel.jsoneditor.model.impl.graph;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;

import java.util.List;


public class NodeGraphCreator
{
    /**
     * if the path contains a node, we build a graph with this as the "center". The graph is directed and contains all ReferenceToObjectInstances that point to this node, and also all references that go out from it
     */
    public static NodeGraph createGraph(ReadableModel model, String path)
    {
        if (path == null)
        {
            return null;
        }
        NodeGraph graph = new NodeGraph();
        graph.insertVertex(path);
        addIncomingReferences(model, path, graph);
        addOutgoingReferences(model, path, graph);
        
        return graph;
    }
    
    public static void addOutgoingReferences(ReadableModel model, String currentPath, NodeGraph graph)
    {
        List<ReferenceToObjectInstance> outgoingReferences = ReferenceHelper.findOutgoingReferences(currentPath, model);
        for (ReferenceToObjectInstance ref : outgoingReferences)
        {
            String toPath = ReferenceHelper.resolveReference(model, ref);
            if (toPath == null)
            {
                continue;
            }
            // check if the edge exists already. If yes, we don't need to add it
            EdgeIdentifier edgeToInsert = new EdgeIdentifier(currentPath, toPath, ref.getRemarks());
            if (graph.edges().stream().noneMatch(edge -> edge.element().equals(edgeToInsert)))
            {
                //no edge with this identifier exists
                if (graph.vertices().stream().noneMatch(stringVertex -> stringVertex.element().getPath().equals(toPath)))
                {
                    graph.insertVertex(toPath);
                }
                graph.insertEdge(currentPath, toPath, edgeToInsert);
            }
        }
    }
    
    private static void addIncomingReferences(ReadableModel model, String path, NodeGraph graph)
    {
        List<ReferenceToObjectInstance> incomingReferences = ReferenceHelper.getReferencesOfObject(model, path);
        for (ReferenceToObjectInstance ref : incomingReferences)
        {
            //the path is the parent object of that reference
            String fromPath = ReferenceHelper.getParentObjectOfReference(model, ref.getPath());
            // check if the edge exists already. If yes, we don't need to add it
            EdgeIdentifier edgeToInsert = new EdgeIdentifier(fromPath, path, ref.getRemarks());
            if (graph.edges().stream().noneMatch(edge -> edge.element().equals(edgeToInsert)))
            {
                if (graph.vertices().stream().noneMatch(stringVertex -> stringVertex.element().getPath().equals(fromPath)))
                {
                    graph.insertVertex(fromPath);
                }
                graph.insertEdge(fromPath, path, edgeToInsert);
            }
        }
    }
    
}
