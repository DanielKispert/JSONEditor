package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class NodeGraphPanel extends SmartGraphPanel<String, EdgeIdentifier>
{
    
    public NodeGraphPanel(ReadableModel model, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile)
    {
        super(model.getJsonAsGraph(path), properties, placementStrategy, cssFile);
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.setAutomaticLayout(true);
        setEdgeLabelProvider(EdgeIdentifier::getName);
        setVertexLabelProvider(s -> model.getNodeForPath(s).getDisplayName());
    }
    
    public Collection<SmartGraphVertex<String>> getVertices()
    {
        return getSmartVertices();
    }
}
