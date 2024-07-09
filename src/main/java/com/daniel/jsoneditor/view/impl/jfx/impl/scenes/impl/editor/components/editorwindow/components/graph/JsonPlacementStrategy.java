package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.util.ArrayList;
import java.util.List;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;


/**
 * this placement strategy attempts to build a hierarchy within the nodes, so that
 */
public class JsonPlacementStrategy implements SmartPlacementStrategy
{
    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> smartGraphPanel)
    {
        if (smartGraphPanel instanceof NodeGraphPanel)
        {
            NodeGraphPanel graphPanel = (NodeGraphPanel) smartGraphPanel;
            List<SmartGraphVertex<String>> vertices = new ArrayList<>(graphPanel.getVertices());
            // place all vertices at the same spot for a test
            for (SmartGraphVertex<String> vertex : vertices)
            {
                vertex.setPosition(width / 2, height / 2);
            }
            
        }
    
    }
}
