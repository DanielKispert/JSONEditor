package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.ReadableModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class NodeGraphPanel extends SmartGraphPanel<String, String>
{
    private static final String PATH_TO_PROPERTIES = "brunomnsilva/smartgraph/smartgraph.properties";
    
    private static final String PATH_TO_CSS = "brunomnsilva/smartgraph/smartgraph.css";
    
    private final ReadableModel model;
    
    
    
    private NodeGraphPanel(ReadableModel model, SmartGraphProperties properties, SmartPlacementStrategy placementStrategy, URI cssFile)
    {
        super(model.getJsonAsGraph(), properties, placementStrategy, cssFile);
        this.model = model;
    }
    
    
    
    public static NodeGraphPanel create(ReadableModel model)
    {
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        InputStream propertiesFile = NodeGraphPanel.class.getClassLoader().getResourceAsStream(PATH_TO_PROPERTIES);
        URI uri;
        try
        {
            uri = NodeGraphPanel.class.getClassLoader().getResource(PATH_TO_CSS).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        NodeGraphPanel graphView = new NodeGraphPanel(model, new SmartGraphProperties(propertiesFile), initialPlacement, uri);
        graphView.setAutomaticLayout(true);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        VBox.setVgrow(graphView, Priority.ALWAYS);
        return graphView;
        
    }
}
