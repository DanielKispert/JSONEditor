package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.net.URI;
import java.util.Collection;
import java.util.function.Consumer;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.graph.EdgeIdentifier;
import com.daniel.jsoneditor.model.impl.graph.NodeGraphCreator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class NodeGraphPanel extends SmartGraphPanel<String, EdgeIdentifier>
{
    private final ReadableModel model;
    
    public NodeGraphPanel(ReadableModel model, String path, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy,
            URI cssFile)
    {
        super(model.getJsonAsGraph(path), properties, placementStrategy, cssFile);
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.setAutomaticLayout(true);
        setEdgeLabelProvider(EdgeIdentifier::getName);
        setVertexLabelProvider(s -> model.getNodeForPath(s).getDisplayName());
        setAutomaticLayoutStrategy(new JsonForcePlacementStrategy());
        setEdgeDoubleClickAction(this::handleEdgeDoubleClick);
        setVertexDoubleClickAction(this::handleVertexDoubleClick);
    }
    
    private void handleEdgeDoubleClick(SmartGraphEdge<EdgeIdentifier, String> edge)
    {
        Graph<String, EdgeIdentifier> graph = getModel();
        //remove the edge and the vertex it points to
        // the vertex to remove is the 2nd in the vertices list
        graph.removeVertex(edge.getUnderlyingEdge().vertices()[1]);
        update();
    }
    
    private void handleVertexDoubleClick(SmartGraphVertex<String> vertex)
    {
        //add all outgoing references of the vertex to our graph
        NodeGraphCreator.addOutgoingReferences(model, vertex.getUnderlyingVertex().element(), (Digraph<String, EdgeIdentifier>) getModel());
        update();
    }
    
    public Collection<SmartGraphVertex<String>> getVertices()
    {
        return getSmartVertices();
    }
    
    
}
