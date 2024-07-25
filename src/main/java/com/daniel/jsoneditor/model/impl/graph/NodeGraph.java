package com.daniel.jsoneditor.model.impl.graph;

import java.util.List;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;


public class NodeGraph extends DigraphEdgeList<NodeIdentifier, EdgeIdentifier>
{
    public NodeGraph()
    {
    }
    
    public void insertVertex(String path)
    {
        super.insertVertex(NodeIdentifierCache.get(path));
    }
    
    public void insertClusterVertex(String path, List<String> clusteredPaths)
    {
        NodeIdentifier nodeIdentifier = NodeIdentifierCache.get(path);
        nodeIdentifier.setCluster(clusteredPaths);
        super.insertVertex(nodeIdentifier);
    }
    
    public void insertEdge(String fromPath, String toPath, EdgeIdentifier edgeToInsert)
    {
        NodeIdentifier fromNode = NodeIdentifierCache.get(fromPath);
        NodeIdentifier toNode = NodeIdentifierCache.get(toPath);
        super.insertEdge(fromNode, toNode, edgeToInsert);
    }
}
