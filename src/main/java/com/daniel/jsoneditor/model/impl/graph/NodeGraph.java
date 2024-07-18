package com.daniel.jsoneditor.model.impl.graph;

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
    
    public void insertEdge(String fromPath, String toPath, EdgeIdentifier edgeToInsert)
    {
        NodeIdentifier fromNode = NodeIdentifierCache.get(fromPath);
        NodeIdentifier toNode = NodeIdentifierCache.get(toPath);
        super.insertEdge(fromNode, toNode, edgeToInsert);
    }
}
