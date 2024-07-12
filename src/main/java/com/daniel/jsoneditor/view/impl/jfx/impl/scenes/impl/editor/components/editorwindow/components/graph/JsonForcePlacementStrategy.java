package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringGravityLayoutStrategy;


/**
 * strategy for dragging around nodes
 */
public class JsonForcePlacementStrategy extends ForceDirectedSpringGravityLayoutStrategy<String>
{
    public JsonForcePlacementStrategy()
    {
        super(50, 1.5, 1, 1, 0.05);
    }
}
