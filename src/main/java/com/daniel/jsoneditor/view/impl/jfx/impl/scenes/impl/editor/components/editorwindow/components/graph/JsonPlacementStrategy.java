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
        List<Set<SmartGraphVertex<NodeIdentifier>>> layers = new ArrayList<>();
        // we first figure out the nodes that have no incoming edges
        Set<SmartGraphVertex<NodeIdentifier>> roots = new HashSet<>();
        for (SmartGraphVertex<NodeIdentifier> vertex : unplacedVertices)
        {
            
            if (graph.incidentEdges(vertex.getUnderlyingVertex()).isEmpty())
            {
                roots.add(vertex);
            }
        }
        Set<SmartGraphVertex<NodeIdentifier>> currentLayer = roots;
        while (!currentLayer.isEmpty())
        {
            layers.add(currentLayer);
            unplacedVertices.removeAll(currentLayer);
            currentLayer = findVerticesOfNextLayer(currentLayer, graph, unplacedVertices);
        }
        if (!unplacedVertices.isEmpty())
        {
            //put the unplaced vertices in the last layer
            // TODO do something better, handle them and circles properly once it comes up
            layers.add(new HashSet<>(unplacedVertices));
            
        }
        
        //set the layer information for every NodeIdentifier. We can only do this after setting
        int layerIndex = 0;
        int layersCount = layers.size();
        for (Set<SmartGraphVertex<NodeIdentifier>> layer : layers)
        {
            // the layers are ordered from top to bottom, with 0 and 1 assigned. For example, with 3 layers, the layers are 0.25, 0.5 and
            // 0.75. For four layers, it is 0.2, 0.4, 0.6 and 0.8
            double layerPercentage = (layerIndex + 1) / (double) (layersCount + 1);
            for (SmartGraphVertex<NodeIdentifier> vertex : layer)
            {
                vertex.getUnderlyingVertex().element().setLayer(layerPercentage);
            }
            layerIndex++;
        }
        
        // Calculate the x and y positions for each vertex based on its layer and the total width
        for (Set<SmartGraphVertex<NodeIdentifier>> layer : layers)
        {
            double xSpacing = width / (layer.size() + 1);
            List<SmartGraphVertex<NodeIdentifier>> layerVerticesList = new ArrayList<>(layer);
            for (int i = 0; i < layerVerticesList.size(); i++)
            {
                SmartGraphVertex<NodeIdentifier> vertex = layerVerticesList.get(i);
                double x = (i + 1) * xSpacing;
                // Use the layer value from NodeIdentifier to calculate the y position
                double layerPercentage = vertex.getUnderlyingVertex().element().getLayer();
                double y = height * layerPercentage;
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
