package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.brunomnsilva.smartgraph.graphview.ForceDirectedLayoutStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode;
import com.daniel.jsoneditor.model.impl.graph.NodeIdentifier;
import javafx.geometry.Point2D;


/**
 * strategy for dragging around nodes
 */
public class JsonForcePlacementStrategy extends ForceDirectedLayoutStrategy<NodeIdentifier>
{
    
    private double repulsiveForce;
    
    private double attractionForce;
    
    private double attractionScale;
    
    private final double acceleration;
    
    /* just a scaling factor so all parameters are, at most, two-digit numbers. */
    private static final double A_THOUSAND = 1000;
    
    private final double horizontalGravity;
    
    private final double verticalGravity;
    
    public JsonForcePlacementStrategy()
    {
        this.repulsiveForce = 50; //default 25
        this.attractionForce = 0.1; //default 3
        this.attractionScale = 100; //default 10
        this.acceleration = 3; //default 0.8
        this.horizontalGravity = 0.05;
        this.verticalGravity = 0.5;
    }
    
    @Override
    public void computeForces(Collection<SmartGraphVertexNode<NodeIdentifier>> nodes, double panelWidth, double panelHeight)
    {
        //calculate attraction and repulsion based on panel size and amount of nodes
        adjustParameters(getSizeOfLargestLayer(nodes), panelWidth);
        
        for (SmartGraphVertexNode<NodeIdentifier> v : nodes)
        {
            for (SmartGraphVertexNode<NodeIdentifier> w : nodes)
            {
                if (v == w)
                {
                    continue;
                }
                
                Point2D force = computeForceBetween(v, w, panelWidth, panelHeight);
                v.addForceVector(force.getX(), force.getY());
            }
        }
        
        // Gravitational pull towards the horizontal center for all nodes
        double centerX = panelWidth / 2;
        
        for (SmartGraphVertexNode<NodeIdentifier> v : nodes)
        {
            // every node has a different vertical center, depending on the layer
            double centerY = panelHeight * v.getUnderlyingVertex().element().getLayer();
            Point2D curPosition = v.getUpdatedPosition();
            double xDifference = centerX - curPosition.getX();
            double yDifference = centerY - curPosition.getY();
            Point2D forceCenter = new Point2D(xDifference * horizontalGravity, yDifference * verticalGravity);
            v.addForceVector(forceCenter.getX(), forceCenter.getY());
        }
    }
    
    private int getSizeOfLargestLayer(Collection<SmartGraphVertexNode<NodeIdentifier>> nodes)
    {
        Map<Double, Integer> layerCountMap = new HashMap<>();
        
        for (SmartGraphVertexNode<NodeIdentifier> node : nodes)
        {
            double layer = node.getUnderlyingVertex().element().getLayer();
            layerCountMap.put(layer, layerCountMap.getOrDefault(layer, 0) + 1);
        }
        
        int maxCount = 0;
        for (int count : layerCountMap.values())
        {
            maxCount = Math.max(maxCount, count);
        }
        
        return maxCount;
    }
    
    public void adjustParameters(int sizeOfLargestLayer, double panelWidth)
    {
        final double maxPixelsPerNode = 250;
        final double minPixelsPerNode = NodeGraphPanel.MIN_PIXELS_PER_NODE;
        
        double pixelsPerNode = panelWidth / sizeOfLargestLayer;
        //wrap pixelsPerNode between maxPixelsPerNode and minPixelsPerNode
        pixelsPerNode = Math.min(maxPixelsPerNode, Math.max(minPixelsPerNode, pixelsPerNode));
        
        // Define the bounds for the repulsive force
        final double lowerBound = 5;
        final double upperBound = 100;
        
        double densityFactor = (maxPixelsPerNode - pixelsPerNode) / (maxPixelsPerNode - minPixelsPerNode);
        
        // Interpolate the repulsive force based on the density factor
        this.repulsiveForce = lowerBound + (1 - densityFactor) * (upperBound - lowerBound);
    }
    
    @Override
    protected Point2D computeForceBetween(SmartGraphVertexNode<NodeIdentifier> v, SmartGraphVertexNode<NodeIdentifier> w, double panelWidth,
            double panelHeight)
    {
        
        Point2D vPosition = v.getUpdatedPosition();
        Point2D wPosition = w.getUpdatedPosition();
        double distance = vPosition.distance(wPosition) - (v.getRadius() + w.getRadius());
        Point2D forceDirection = wPosition.subtract(vPosition).normalize();
        
        if (distance < 1)
        {
            distance = 1;
        }
        
        // attractive force
        Point2D attraction;
        if (v.isAdjacentTo(w))
        {
            double attraction_factor = attractionForce * Math.log(distance / attractionScale);
            attraction = forceDirection.multiply(attraction_factor);
        }
        else
        {
            attraction = new Point2D(0, 0);
        }
        
        // repelling force
        double repulsive_factor = repulsiveForce * A_THOUSAND / (distance * distance);
        Point2D repulsion = forceDirection.multiply(-repulsive_factor);
        
        // combine forces
        Point2D totalForce = new Point2D(attraction.getX() + repulsion.getX(), attraction.getY() + repulsion.getY());
        
        return totalForce.multiply(acceleration);
    }
}
