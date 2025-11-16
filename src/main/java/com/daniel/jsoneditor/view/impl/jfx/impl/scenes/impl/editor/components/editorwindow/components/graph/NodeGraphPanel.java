package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import java.util.Set;
import java.util.stream.Collectors;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.impl.graph.NodeGraph;

import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class NodeGraphPanel extends SmartGraphPanel<NodeIdentifier, EdgeIdentifier>
{
    public static final int MIN_PIXELS_PER_NODE = 50;
    
    public static final int MAX_NAME_LENGTH = 30;
    
    private final SmartPlacementStrategy placementStrategy;
    
    private final ReadableModel model;
    
    private final String path;
    
    private final Set<String> allowedEdgeNames;
    
    private Runnable filterUpdateCallback;
    
    public NodeGraphPanel(ReadableModel model, Controller controller, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile, Set<String> allowedEdgeNames)
    {
        super(model.getJsonAsGraph(path, allowedEdgeNames), properties, placementStrategy, cssFile);
        this.placementStrategy = placementStrategy;
        this.model = model;
        this.path = path;
        this.allowedEdgeNames = allowedEdgeNames;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.setAutomaticLayout(true);
        setEdgeLabelProvider(EdgeIdentifier::getName);
        setVertexLabelProvider(s -> {
            String unshortenedName = model.getNodeForPath(s.getPath()).getDisplayName();
            int length = unshortenedName.length();
            return length > (MAX_NAME_LENGTH - 3) ? "..." + unshortenedName.substring(length - (MAX_NAME_LENGTH - 3)) : unshortenedName;
        });
        setAutomaticLayoutStrategy(new JsonForcePlacementStrategy());
        setEdgeDoubleClickAction(this::handleEdgeDoubleClick);
        setVertexDoubleClickAction(this::handleVertexDoubleClick);
        String clusterSymbol = controller.getSettingsController().getClusterShape();
        setVertexShapeTypeProvider(nodeIdentifier -> nodeIdentifier.isCluster() ? clusterSymbol : "circle");
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
     * Remove the vertex that the edge is pointing to, and all vertices that that vertex, but no other vertex that is not being deleted, is pointing to, recursively.
     * @param edge The edge whose target vertex should be removed
     */
    private void handleEdgeDoubleClick(SmartGraphEdge<EdgeIdentifier, NodeIdentifier> edge)
    {
        NodeGraph graph = (NodeGraph) getModel();
        Vertex<NodeIdentifier> targetVertex = edge.getUnderlyingEdge().vertices()[1];
        if (!targetVertex.element().isCluster())
        {
            removeVertexAndConnected(graph, targetVertex);
            update();
        }

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
            Vertex<NodeIdentifier> targetVertex = outboundEdge.vertices()[1];
            if (graph.incidentEdges(targetVertex).size() == 1)
            {
                collectVerticesToRemove(graph, targetVertex, verticesToRemove);
            }
            verticesToRemove.add(targetVertex);
        }
    }
    
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
        NodeIdentifier nodeId = vertex.getUnderlyingVertex().element();
        if (!nodeId.isCluster())
        {
            expandVertex(nodeId.getPath());
        }
    }
    
    public Collection<SmartGraphVertex<NodeIdentifier>> getVertices()
    {
        return getSmartVertices();
    }
    
    public Collection<String> getAllEdgeNames()
    {
        return model.getJsonAsGraph(path, null).edges().stream()
                .map(edge -> edge.element().getName())
                .sorted()
                .collect(Collectors.toList());
    }
    
    public void setFilterUpdateCallback(Runnable callback)
    {
        this.filterUpdateCallback = callback;
    }
    
    private void expandVertex(String vertexPath)
    {
        NodeGraph currentGraph = (NodeGraph) getModel();
        NodeGraph expandedGraph = model.getJsonAsGraph(vertexPath, allowedEdgeNames);
        
        Set<String> currentEdgeNames = currentGraph.edges().stream()
                .map(edge -> edge.element().getName())
                .collect(Collectors.toSet());
        
        Set<String> newEdgeNames = expandedGraph.edges().stream()
                .map(edge -> edge.element().getName())
                .filter(name -> !currentEdgeNames.contains(name))
                .collect(Collectors.toSet());
        
        for (Vertex<NodeIdentifier> vertex : expandedGraph.vertices())
        {
            if (currentGraph.vertices().stream().noneMatch(v -> v.element().getPath().equals(vertex.element().getPath())))
            {
                if (vertex.element().isCluster())
                {
                    currentGraph.insertClusterVertex(vertex.element().getPath(), vertex.element().getClusterPaths());
                }
                else
                {
                    currentGraph.insertVertex(vertex.element().getPath());
                }
            }
        }
        
        for (Edge<EdgeIdentifier, NodeIdentifier> edge : expandedGraph.edges())
        {
            if (currentGraph.edges().stream().noneMatch(e -> e.element().equals(edge.element())))
            {
                currentGraph.insertEdge(edge.element().getFrom(), edge.element().getTo(), edge.element());
            }
        }
        
        if (!newEdgeNames.isEmpty() && filterUpdateCallback != null)
        {
            filterUpdateCallback.run();
        }
        
        update();
    }
    
}
