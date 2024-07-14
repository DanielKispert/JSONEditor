package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;


/**
 * this placement strategy attempts to build a hierarchy within the nodes, so that
 */
public class JsonPlacementStrategy implements SmartPlacementStrategy
{
    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> smartGraphPanel) {
        if (!(smartGraphPanel instanceof NodeGraphPanel)) {
            return;
        }
        
        NodeGraphPanel graphPanel = (NodeGraphPanel) smartGraphPanel;
        Digraph<String, EdgeIdentifier> graph = (Digraph<String, EdgeIdentifier>) graphPanel.getModel();
        List<SmartGraphVertex<String>> unplacedVertices = new ArrayList<>(graphPanel.getVertices());
        // we first figure out the nodes that have no incoming edges
        List<SmartGraphVertex<String>> roots = new ArrayList<>();
        for (SmartGraphVertex<String> vertex : unplacedVertices)
        {
            if (graph.incidentEdges(vertex.getUnderlyingVertex()).isEmpty())
            {
                roots.add(vertex);
            }
        }
        if (roots.isEmpty())
        {
            //TODO
        }
        // from every root, place the adjacent edges in the next layer
        
        
        Map<Integer, List<SmartGraphVertex<String>>> layers = assignLayers(unplacedVertices);
        
        double layerHeight = height / layers.size();
        for (Map.Entry<Integer, List<SmartGraphVertex<String>>> entry : layers.entrySet())
        {
            int layer = entry.getKey();
            List<SmartGraphVertex<String>> layerVertices = entry.getValue();
            double xSpacing = width / (layerVertices.size() + 1);
            for (int i = 0; i < layerVertices.size(); i++)
            {
                SmartGraphVertex<String> vertex = layerVertices.get(i);
                double x = (i + 1) * xSpacing;
                double y = (layer + 0.5) * layerHeight;
                vertex.setPosition(x, y);
            }
        }
    }
    
    private Map<Integer, List<SmartGraphVertex<String>>> assignLayers(List<SmartGraphVertex<String>> vertices) {
        // Implement layer assignment based on your graph's structure
        // This is a placeholder and should be replaced with actual logic
        Map<Integer, List<SmartGraphVertex<String>>> layers = new HashMap<>();
        // Example logic to populate layers map
        return layers;
    }
}
