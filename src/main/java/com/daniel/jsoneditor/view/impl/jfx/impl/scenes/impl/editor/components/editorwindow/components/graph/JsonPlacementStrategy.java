package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.util.*;
import java.util.stream.Collectors;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.impl.graph.NodeGraph;
import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;


/**
 * this placement strategy attempts to build a hierarchy within the nodes, so that
 */
public class JsonPlacementStrategy implements SmartPlacementStrategy
{
    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> smartGraphPanel)
    {
        if (!(smartGraphPanel instanceof NodeGraphPanel))
        {
            return;
        }
        
        NodeGraphPanel graphPanel = (NodeGraphPanel) smartGraphPanel;
        NodeGraph graph = (NodeGraph) graphPanel.getModel();
        List<SmartGraphVertex<NodeIdentifier>> unplacedVertices = new ArrayList<>(graphPanel.getVertices());
        Map<Integer, Set<SmartGraphVertex<NodeIdentifier>>> layers = new HashMap<>();
        // we first figure out the nodes that have no incoming edges
        Set<SmartGraphVertex<NodeIdentifier>> roots = new HashSet<>();
        for (SmartGraphVertex<NodeIdentifier> vertex : unplacedVertices)
        {
            
            if (graph.incidentEdges(vertex.getUnderlyingVertex()).isEmpty())
            {
                roots.add(vertex);
            }
        }
        int currentLayerIndex = 0;
        Set<SmartGraphVertex<NodeIdentifier>> currentLayer = roots;
        while (!currentLayer.isEmpty())
        {
            layers.put(currentLayerIndex, currentLayer);
            unplacedVertices.removeAll(currentLayer);
            currentLayer = findVerticesOfNextLayer(currentLayer, graph, unplacedVertices);
            currentLayerIndex++;
        }
        
        
        double layerHeight = height / layers.size();
        for (Map.Entry<Integer, Set<SmartGraphVertex<NodeIdentifier>>> entry : layers.entrySet())
        {
            int layer = entry.getKey();
            Set<SmartGraphVertex<NodeIdentifier>> layerVertices = entry.getValue();
            double xSpacing = width / (layerVertices.size() + 1);
            List<SmartGraphVertex<NodeIdentifier>> layerVerticesList = new ArrayList<>(layerVertices);
            for (int i = 0; i < layerVerticesList.size(); i++)
            {
                SmartGraphVertex<NodeIdentifier> vertex = layerVerticesList.get(i);
                double x = (i + 1) * xSpacing;
                double y = (layer + 0.5) * layerHeight;
                vertex.setPosition(x, y);
            }
        }
    }
    
    private static Set<SmartGraphVertex<NodeIdentifier>> findVerticesOfNextLayer(Set<SmartGraphVertex<NodeIdentifier>> currentLayer, NodeGraph graph, List<SmartGraphVertex<NodeIdentifier>> unplacedVertices)
    {
        Set<SmartGraphVertex<NodeIdentifier>> nextLayer = new HashSet<>();
        for (SmartGraphVertex<NodeIdentifier> vertex : currentLayer)
        {
            Collection<Edge<EdgeIdentifier, NodeIdentifier>> edges = graph.outboundEdges(vertex.getUnderlyingVertex());
            for (Edge<EdgeIdentifier, NodeIdentifier> edge : edges)
            {
                NodeIdentifier targetVertexId = edge.vertices()[1].element();
                // Find the SmartGraphVertex in unplacedVertices that matches the targetVertexId
                unplacedVertices.stream()
                        .filter(v -> v.getUnderlyingVertex().element().equals(targetVertexId))
                        .findFirst().ifPresent(nextLayer::add);
            }
        }
        return nextLayer;
    }
}
