package com.daniel.jsoneditor.model.impl.graph;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class NodeGraphCreator
{
    private static final Logger logger = LoggerFactory.getLogger(NodeGraphCreator.class);

    /**
     * Creates a graph with incoming and outgoing references for the given path
     * @param allowedEdgeNames set of edge names to include, null means include all
     */
    public static NodeGraph createGraph(ReadableModel model, String path, Set<String> allowedEdgeNames)
    {
        logger.debug("Graph request - path: {}, allowedEdgeNames: {}", path, allowedEdgeNames);
        
        if (path == null)
        {
            return new NodeGraph();
        }
        final NodeGraph graph = new NodeGraph();
        graph.insertVertex(path);
        addIncomingReferences(model, path, graph, allowedEdgeNames);
        addOutgoingReferences(model, path, graph, allowedEdgeNames);
        return graph;
    }
    

    
    private static void addClusterNodeToGraph(NodeGraph graph, String path, List<String> clusteredPaths)
    {
        if (graph.vertices().stream().noneMatch(vertex -> vertex.element().getPath().equals(path)))
        {
            graph.insertClusterVertex(path, clusteredPaths);
        }
    }
    
    private static void addEdgeToGraph(NodeGraph graph, EdgeIdentifier edge)
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
    

    
    private static void addIncomingReferences(ReadableModel model, String path, NodeGraph graph, Set<String> allowedEdgeNames)
    {
        List<ReferenceToObjectInstance> incomingReferences = ReferenceHelper.getReferencesOfObject(model, path);
        for (ReferenceToObjectInstance ref : incomingReferences)
        {
            if (allowedEdgeNames == null || allowedEdgeNames.contains(ref.getRemarks()))
            {
                String fromPath = ReferenceHelper.getParentObjectOfReference(model, ref.getPath());
                EdgeIdentifier edgeToInsert = new EdgeIdentifier(fromPath, path, ref.getRemarks());
                addEdgeToGraph(graph, edgeToInsert);
            }
        }
    }
    
    private static void addOutgoingReferences(ReadableModel model, String currentPath, NodeGraph graph, Set<String> allowedEdgeNames)
    {
        List<ReferenceToObjectInstance> references = ReferenceHelper.findOutgoingReferences(currentPath, model);
        Map<String, List<ReferenceToObjectInstance>> referencesGroupedByRemarks = new HashMap<>();
        
        for (ReferenceToObjectInstance ref : references)
        {
            if (allowedEdgeNames == null || allowedEdgeNames.contains(ref.getRemarks()))
            {
                referencesGroupedByRemarks.computeIfAbsent(ref.getRemarks(), k -> new ArrayList<>()).add(ref);
            }
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
                JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
                if (parentNode.isArray() && refs.size() > 1)
                {
                    List<String> toPaths = new ArrayList<>();
                    for (ReferenceToObjectInstance ref : refs)
                    {
                        String toPath = ReferenceHelper.resolveReference(model, ref);
                        if (toPath == null)
                        {
                            continue;
                        }
                        toPaths.add(toPath);
                    }
                    EdgeIdentifier edgeToCluster = new EdgeIdentifier(currentPath, parentPath, remarks);
                    addClusterNodeToGraph(graph, parentPath, toPaths);
                    addEdgeToGraph(graph, edgeToCluster);
                }
                else
                {
                    for (ReferenceToObjectInstance ref : refs)
                    {
                        String toPath = ReferenceHelper.resolveReference(model, ref);
                        if (toPath == null)
                        {
                            continue;
                        }
                        EdgeIdentifier edge = new EdgeIdentifier(currentPath, toPath, ref.getRemarks());
                        addEdgeToGraph(graph, edge);
                    }
                }
            }
        }
    }
    

}
