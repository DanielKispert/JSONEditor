package com.daniel.jsoneditor.model.impl.graph;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NodeGraphCreator
{
    /**
     * if the path contains a node, we build a graph with this as the "center". The graph is directed and contains all
     * ReferenceToObjectInstances that point to this node, and also all references that go out from it
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
        List<ReferenceToObjectInstance> references = ReferenceHelper.findOutgoingReferences(currentPath, model);
        Map<String, List<ReferenceToObjectInstance>> referencesGroupedByRemarks = new HashMap<>();
        
        for (ReferenceToObjectInstance ref : references)
        {
            referencesGroupedByRemarks.computeIfAbsent(ref.getRemarks(), k -> new ArrayList<>()).add(ref);
        }
        
        for (Map.Entry<String, List<ReferenceToObjectInstance>> entry : referencesGroupedByRemarks.entrySet())
        {
            String remarks = entry.getKey();
            List<ReferenceToObjectInstance> referencesWithSameRemarks = entry.getValue();
            Map<String, List<ReferenceToObjectInstance>> referencesWithSameParent = new HashMap<>();
            for (ReferenceToObjectInstance ref : referencesWithSameRemarks)
            {
                String toPath = ReferenceHelper.resolveReference(model, ref);
                if (toPath == null)
                {
                    continue;
                }
                String parentPath = PathHelper.getParentPath(toPath);
                referencesWithSameParent.computeIfAbsent(parentPath, k -> new ArrayList<>()).add(ref);
            }
            for (Map.Entry<String, List<ReferenceToObjectInstance>> refEntry : referencesWithSameParent.entrySet())
            {
                String parentPath = refEntry.getKey();
                List<ReferenceToObjectInstance> refs = refEntry.getValue();
                // if the parent points to an array
                JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
                if (parentNode.isArray() && refs.size() > 1)
                {
                    //there are more than one references with the same parent and remarks, so we cluster them in a common node
                    EdgeIdentifier edgeToCluster = new EdgeIdentifier(currentPath, parentPath, remarks);
                    addEdgeToGraph(edgeToCluster, graph);
                }
                else
                {
                    // we add the references one by one since either their parent isn't an array or there is only one reference
                    for (ReferenceToObjectInstance ref : refs)
                    {
                        String toPath = ReferenceHelper.resolveReference(model, ref);
                        if (toPath == null)
                        {
                            continue;
                        }
                        EdgeIdentifier edge = new EdgeIdentifier(currentPath, toPath, ref.getRemarks());
                        addEdgeToGraph(edge, graph);
                    }
                }
            }
        }
    }
    
    private static void addEdgeToGraph(EdgeIdentifier edge, NodeGraph graph)
    {
        if (graph.edges().stream().noneMatch(existingEdge -> existingEdge.element().equals(edge)))
        {
            if (graph.vertices().stream().noneMatch(vertex -> vertex.element().getPath().equals(edge.getFrom())))
            {
                graph.insertVertex(edge.getFrom());
            }
            if (graph.vertices().stream().noneMatch(vertex -> vertex.element().getPath().equals(edge.getTo())))
            {
                graph.insertVertex(edge.getTo());
            }
            graph.insertEdge(edge.getFrom(), edge.getTo(), edge);
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
