package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringGravityLayoutStrategy;


/**
 * strategy for dragging around nodes
 */
public class JsonForcePlacementStrategy extends ForceDirectedSpringGravityLayoutStrategy<String>
{
    public JsonForcePlacementStrategy()
    {
        super(repulsiveForce, attractionForce, attractionScale, acceleration, 0.01);
    }
}
