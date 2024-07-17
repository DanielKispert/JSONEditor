package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringGravityLayoutStrategy;
import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringSystemLayoutStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode;
import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;
import javafx.geometry.Point2D;

import java.util.Collection;


/**
 * strategy for dragging around nodes
 */
public class JsonForcePlacementStrategy extends ForceDirectedSpringSystemLayoutStrategy<NodeIdentifier>
{
    private final double gravity;
    
    public JsonForcePlacementStrategy()
    {
        super(50, 1.0, 1, 1);
        this.gravity = 0.05;
    }
    
    @Override
    public void computeForces(Collection<SmartGraphVertexNode<NodeIdentifier>> nodes, double panelWidth, double panelHeight)
    {
        // Attractive and repulsive forces
        for (SmartGraphVertexNode<NodeIdentifier> v : nodes) {
            for (SmartGraphVertexNode<NodeIdentifier> w : nodes) {
                if(v == w) continue;
                
                Point2D force = computeForceBetween(v, w, panelWidth, panelHeight);
                v.addForceVector(force.getX(), force.getY());
            }
        }
        
        // Gravitational pull towards the center for all nodes
        double centerX = panelWidth / 2;
        double centerY = panelHeight / 2;
        
        for (SmartGraphVertexNode<NodeIdentifier> v : nodes) {
            Point2D curPosition = v.getUpdatedPosition();
            Point2D forceCenter = new Point2D(centerX - curPosition.getX(), centerY - curPosition.getY())
                                          .multiply(gravity);
            
            v.addForceVector(forceCenter.getX(), forceCenter.getY());
        }
    }
}
