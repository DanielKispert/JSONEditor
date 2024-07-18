package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.impl.graph.NodeGraph;
import com.daniel.jsoneditor.model.impl.graph.NodeGraphCreator;
import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class NodeGraphPanel extends SmartGraphPanel<NodeIdentifier, EdgeIdentifier>
{
    private final ReadableModel model;
    
    public NodeGraphPanel(ReadableModel model, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile)
    {
        super(model.getJsonAsGraph(path), properties, placementStrategy, cssFile);
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.setAutomaticLayout(true);
        setEdgeLabelProvider(EdgeIdentifier::getName);
        setVertexLabelProvider(s -> model.getNodeForPath(s.getPath()).getDisplayName());
        setAutomaticLayoutStrategy(new JsonForcePlacementStrategy());
        setEdgeDoubleClickAction(this::handleEdgeDoubleClick);
        setVertexDoubleClickAction(this::handleVertexDoubleClick);
    }
    
    /**
     * remove the vertex that the edge is pointing to, and all vertices that that vertex, but no other vertex that is not being deleted, is pointing to, recursively.
     * @param
     */
    private void handleEdgeDoubleClick(SmartGraphEdge<EdgeIdentifier, NodeIdentifier> edge)
    {
        NodeGraph graph = (NodeGraph) getModel();
        Vertex<NodeIdentifier> targetVertex = edge.getUnderlyingEdge().vertices()[1]; // The 2nd vertex in the edge is the "target" of the edge
        removeVertexAndConnected(graph, targetVertex);
        update();
    }
    
    private void removeVertexAndConnected(NodeGraph graph, Vertex<NodeIdentifier> vertexId)
    {
        Collection<Vertex<NodeIdentifier>> verticesToRemove = new HashSet<>();
        collectVerticesToRemove(graph, vertexId, verticesToRemove);
        verticesToRemove.forEach(graph::removeVertex);
    }
    
    private void collectVerticesToRemove(NodeGraph graph, Vertex<NodeIdentifier> startingVertex,
            Collection<Vertex<NodeIdentifier>> verticesToRemove)
    {
        for (Edge<EdgeIdentifier, NodeIdentifier> outboundEdge : graph.outboundEdges(startingVertex))
        {
            // remove the vertex this edge points to if this is the only inbound edge this vertex had (= it would be orphaned by removing it)
            Vertex<NodeIdentifier> targetVertex = outboundEdge.vertices()[1];
            if (graph.incidentEdges(targetVertex).size() == 1)
            {
                collectVerticesToRemove(graph, targetVertex, verticesToRemove);
                verticesToRemove.add(targetVertex);
            }
            verticesToRemove.add(targetVertex);
        }
    }
    
    private void handleVertexDoubleClick(SmartGraphVertex<NodeIdentifier> vertex)
    {
        //add all outgoing references of the vertex to our graph
        NodeGraphCreator.addOutgoingReferences(model, vertex.getUnderlyingVertex().element().getPath(), (NodeGraph) getModel());
        update();
    }
    
    public Collection<SmartGraphVertex<NodeIdentifier>> getVertices()
    {
        return getSmartVertices();
    }
    
    
}
