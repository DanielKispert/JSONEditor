package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

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

/**
    * Graph view for the editor window, VBox wrapper so we can initialize the graph inside
 */
public class NodeGraphPanelCreator
{
    
    private static final String PATH_TO_PROPERTIES = "brunomnsilva/smartgraph/smartgraph.properties";
    
    private static final String PATH_TO_CSS = "brunomnsilva/smartgraph/smartgraph.css";
    
    private final ReadableModel model;
    
    public NodeGraphPanelCreator(ReadableModel model)
    {
        this.model = model;
    }
    
    public SmartGraphPanel<String, String> makeView()
    {
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        InputStream propertiesFile = getClass().getClassLoader().getResourceAsStream(PATH_TO_PROPERTIES);
        URI uri;
        try
        {
            uri = getClass().getClassLoader().getResource(PATH_TO_CSS).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(model.getJsonAsGraph(), new SmartGraphProperties(propertiesFile), initialPlacement, uri);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        VBox.setVgrow(graphView, Priority.ALWAYS);
        return graphView;
    }

}
