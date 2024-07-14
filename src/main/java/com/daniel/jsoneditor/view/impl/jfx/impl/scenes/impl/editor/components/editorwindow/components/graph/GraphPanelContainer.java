package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.NavbarElement;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class GraphPanelContainer extends HBox implements NavbarElement
{
    private static final String PATH_TO_PROPERTIES = "brunomnsilva/smartgraph/smartgraph.properties";
    
    private static final String PATH_TO_CSS = "brunomnsilva/smartgraph/smartgraph.css";
    
    private final ReadableModel model;
    
    private final JsonEditorNavbar navbar;
    
    private NodeGraphPanel graphView;
    
    private final SmartGraphProperties properties;
    
    private final SmartPlacementStrategy initialPlacement;
    
    private final URI cssFile;
    
    private String selectedPath;
    
    private GraphPanelContainer(JsonEditorNavbar navbar, ReadableModel model, SmartGraphProperties properties, URI cssFile)
    {
        this.initialPlacement = new JsonPlacementStrategy();
        //this.initialPlacement = new SmartCircularSortedPlacementStrategy();
        this.cssFile = cssFile;
        this.navbar = navbar;
        this.properties = properties;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.selectPath("");
    }
    
    
    
    public static GraphPanelContainer create(JsonEditorNavbar navbar, ReadableModel model)
    {
        InputStream propertiesFile = GraphPanelContainer.class.getClassLoader().getResourceAsStream(PATH_TO_PROPERTIES);
        URI uri;
        try
        {
            uri = GraphPanelContainer.class.getClassLoader().getResource(PATH_TO_CSS).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        GraphPanelContainer graphView = new GraphPanelContainer(navbar, model, new SmartGraphProperties(propertiesFile), uri);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        VBox.setVgrow(graphView, Priority.ALWAYS);
        return graphView;
        
    }
    
    @Override
    public void updateView()
    {
        this.graphView = new NodeGraphPanel(model, selectedPath, properties, initialPlacement, cssFile);
        this.getChildren().clear();
        this.getChildren().add(graphView);
        Platform.runLater(graphView::init);
        Platform.runLater(graphView::update); //hacky hack so the labels are properly loaded
    }
    
    @Override
    public void selectPath(String path)
    {
        if (!Objects.equals(selectedPath, path))
        {
            selectedPath = path;
            updateView();
        }
    }
    
    @Override
    public void updateSingleElement(String path)
    {
    
    }
}
