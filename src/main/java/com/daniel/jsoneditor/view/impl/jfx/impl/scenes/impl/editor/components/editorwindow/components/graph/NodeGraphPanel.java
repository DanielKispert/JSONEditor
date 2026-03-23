package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import com.daniel.jsoneditor.controller.settings.SettingsController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeGraphPanel extends SmartGraphPanel<NodeIdentifier, EdgeIdentifier>
{
    private static final Logger logger = LoggerFactory.getLogger(NodeGraphPanel.class);
    
    public static final int MIN_PIXELS_PER_NODE = 50;
    
    public static final int MAX_NAME_LENGTH = 30;
    
    private final SmartPlacementStrategy placementStrategy;
    
    private final ReadableModel model;
    
    private final SettingsController settingsController;
    
    private final String path;
    
    private final Set<String> allowedEdgeNames;
    
    private java.util.function.Consumer<Collection<String>> filterUpdateCallback;
    
    public NodeGraphPanel(ReadableModel model, SettingsController settingsController, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile, Set<String> allowedEdgeNames)
    {
        super(createInitialGraph(model, settingsController, path, allowedEdgeNames), properties, placementStrategy, cssFile);
        this.placementStrategy = placementStrategy;
        this.model = model;
        this.settingsController = settingsController;
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
        String clusterSymbol = settingsController.getClusterShape();
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
        try
        {
            NodeIdentifier nodeId = vertex.getUnderlyingVertex().element();
            logger.debug("Vertex double-clicked: path={}, isCluster={}", nodeId.getPath(), nodeId.isCluster());
            if (nodeId.isCluster())
            {
                dissolveCluster(vertex.getUnderlyingVertex());
            }
            else
            {
                expandVertex(nodeId.getPath());
            }
        }
        catch (Exception e)
        {
            logger.error("Error handling vertex double click", e);
        }
    }
    
    /**
     * Replaces a cluster vertex with individual vertices for each path in the cluster. Incoming edges to the cluster are replaced with
     * individual edges to each new vertex (keeping the same edge name).
     */
    private void dissolveCluster(Vertex<NodeIdentifier> clusterVertex)
    {
        NodeGraph graph = (NodeGraph) getModel();
        NodeIdentifier clusterId = clusterVertex.element();
        List<String> clusterPaths = clusterId.getClusterPaths();
        
        // Collect incoming edges before removing the cluster (we need the source vertices and edge names)
        Collection<Edge<EdgeIdentifier, NodeIdentifier>> incomingEdges = new ArrayList<>(graph.incidentEdges(clusterVertex));
        // Filter to only edges pointing TO the cluster (incident includes both directions in a digraph)
        List<EdgeIdentifier> edgesPointingToCluster = incomingEdges.stream()
                .map(Edge::element)
                .filter(e -> e.getTo().equals(clusterId.getPath()))
                .collect(Collectors.toList());
        
        graph.removeVertex(clusterVertex);
        
        // Insert individual vertices for each path in the cluster
        for (String path : clusterPaths)
        {
            if (graph.vertices().stream().noneMatch(v -> v.element().getPath().equals(path)))
            {
                graph.insertVertex(path);
            }
        }
        
        // Re-create edges from each original source to each individual vertex
        for (EdgeIdentifier originalEdge : edgesPointingToCluster)
        {
            for (String path : clusterPaths)
            {
                EdgeIdentifier newEdge = new EdgeIdentifier(originalEdge.getFrom(), path, originalEdge.getName());
                if (graph.edges().stream().noneMatch(e -> e.element().equals(newEdge)))
                {
                    graph.insertEdge(originalEdge.getFrom(), path, newEdge);
                }
            }
        }
        
        update();
    }
    
    public Collection<SmartGraphVertex<NodeIdentifier>> getVertices()
    {
        return getSmartVertices();
    }
    
    private static NodeGraph createInitialGraph(ReadableModel model, SettingsController settingsController, String path, Set<String> allowedEdgeNames)
    {
        logGraphRequest(settingsController, path, allowedEdgeNames);
        return model.getJsonAsGraph(path, allowedEdgeNames);
    }
    
    private static void logGraphRequest(SettingsController settingsController, String path, Set<String> allowedEdgeNames)
    {
        if (settingsController.isLogGraphRequests())
        {
            logger.debug("Graph request - path: {}, allowedEdgeNames: {}", path, allowedEdgeNames);
        }
    }
    
    public Collection<String> getAllEdgeNames()
    {
        return getModel().edges().stream()
                .map(edge -> edge.element().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public void setFilterUpdateCallback(java.util.function.Consumer<Collection<String>> callback)
    {
        this.filterUpdateCallback = callback;
    }
    
    private void expandVertex(String vertexPath)
    {
        NodeGraph currentGraph = (NodeGraph) getModel();
        // Always fetch the unfiltered graph so we discover all edge names for the filter popup
        logGraphRequest(settingsController, vertexPath, null);
        NodeGraph unfilteredGraph = model.getJsonAsGraph(vertexPath, null);
        
        Set<String> currentEdgeNames = currentGraph.edges().stream()
                .map(edge -> edge.element().getName())
                .collect(Collectors.toSet());
        
        Set<String> allNewEdgeNames = unfilteredGraph.edges().stream()
                .map(edge -> edge.element().getName())
                .filter(name -> !currentEdgeNames.contains(name))
                .collect(Collectors.toSet());
        
        // Determine which edges to actually add to the visible graph based on the current filter
        NodeGraph graphToMerge;
        if (allowedEdgeNames == null)
        {
            // "show all" mode - merge everything
            graphToMerge = unfilteredGraph;
        }
        else
        {
            // Filtered mode - only merge edges matching the current filter
            logGraphRequest(settingsController, vertexPath, allowedEdgeNames);
            graphToMerge = model.getJsonAsGraph(vertexPath, allowedEdgeNames);
        }
        
        for (Vertex<NodeIdentifier> vertex : graphToMerge.vertices())
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
        
        for (Edge<EdgeIdentifier, NodeIdentifier> edge : graphToMerge.edges())
        {
            if (currentGraph.edges().stream().noneMatch(e -> e.element().equals(edge.element())))
            {
                currentGraph.insertEdge(edge.element().getFrom(), edge.element().getTo(), edge.element());
            }
        }
        
        if (!allNewEdgeNames.isEmpty() && filterUpdateCallback != null)
        {
            filterUpdateCallback.accept(allNewEdgeNames);
        }
        
        update();
    }
    
}
