package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.util.ArrayList;
import java.util.List;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;


public class JsonPlacementStrategy implements SmartPlacementStrategy
{
    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> smartGraphPanel)
    {
        if (smartGraphPanel instanceof NodeGraphPanel) {
            NodeGraphPanel graphPanel = (NodeGraphPanel) smartGraphPanel;
            List<SmartGraphVertex<String>> vertices = new ArrayList<>(graphPanel.getVertices());
            
            
        }
    
    }
}
