package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.impl.graph.NodeGraph;
import com.daniel.jsoneditor.model.impl.graph.NodeGraphCreator;
import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class NodeGraphPanel extends SmartGraphPanel<NodeIdentifier, EdgeIdentifier>
{
    
    public static final int MIN_PIXELS_PER_NODE = 100;
    
    public static final int MAX_NAME_LENGTH = 30;
    
    private final SmartPlacementStrategy placementStrategy;
    
    private final ReadableModel model;
    
    public NodeGraphPanel(ReadableModel model, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile)
    {
        super(model.getJsonAsGraph(path), properties, placementStrategy, cssFile);
        this.placementStrategy = placementStrategy;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.setAutomaticLayout(true);
        setEdgeLabelProvider(EdgeIdentifier::getName);
        setVertexLabelProvider(s ->
        {
            String unshortenedName = model.getNodeForPath(s.getPath()).getDisplayName();
            int length = unshortenedName.length();
            //the shortened name is the last X-3 characters of the name, with an ellipsis in front
            return length > (MAX_NAME_LENGTH - 3) ? "..." + unshortenedName.substring(length - (MAX_NAME_LENGTH - 3)) : unshortenedName;
        });
        setAutomaticLayoutStrategy(new JsonForcePlacementStrategy());
        setEdgeDoubleClickAction(this::handleEdgeDoubleClick);
        setVertexDoubleClickAction(this::handleVertexDoubleClick);
        setVertexShapeTypeProvider(nodeIdentifier -> nodeIdentifier.isCluster() ? "hexagon" : "circle");
        setVertexRadiusProvider(nodeIdentifier -> nodeIdentifier.isCluster() ? 20 + (nodeIdentifier.getClusterPaths().size() / 2.0) : 15);
    }
    
    private void updateTooltipsOfVertices()
    {
        this.getVertices().forEach(vertex ->
        {
            NodeIdentifier nodeIdentifier = vertex.getUnderlyingVertex().element();
            Tooltip tooltip = nodeIdentifier.isCluster() ? TooltipHelper.makeTooltipFromPaths(model, nodeIdentifier.getClusterPaths())
                    : TooltipHelper.makeTooltipFromPath(model, nodeIdentifier.getPath());
            SmartStylableNode node = this.getStylableVertex(vertex.getUnderlyingVertex());
            SmartGraphVertexNode<NodeIdentifier> v = (SmartGraphVertexNode<NodeIdentifier>) node;
            Tooltip.install(v, tooltip);
        });
    }
    
    private void updateClusterStyling()
    {
        this.getVertices().forEach(vertex -> {
            if (vertex.getUnderlyingVertex().element().isCluster())
            {
                vertex.setStyleClass("clusterVertex");
            }
        });
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
    
    /**
     * overridden update method also updates the vertex positions and tooltips. Otherwise changes after the panel generation (like expanding vertexes) would not get a layer or tooltips
     */
    @Override
    public void update()
    {
        super.update();
        Platform.runLater(() -> {
            placementStrategy.place(widthProperty().doubleValue(), heightProperty().doubleValue(), NodeGraphPanel.this);
            updateTooltipsOfVertices();
            updateClusterStyling();
        });
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
