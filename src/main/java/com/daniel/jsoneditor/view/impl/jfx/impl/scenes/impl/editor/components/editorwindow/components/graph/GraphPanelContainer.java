package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
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
    
    private final Controller controller;
    
    private NodeGraphPanel graphView;
    
    private final SmartGraphProperties properties;
    
    private final SmartPlacementStrategy initialPlacement;
    
    private final URI cssFile;
    
    private String selectedPath;
    
    private GraphPanelContainer(JsonEditorNavbar navbar, Controller controller, ReadableModel model, SmartGraphProperties properties, URI cssFile)
    {
        this.initialPlacement = new JsonPlacementStrategy();
        this.cssFile = cssFile;
        this.controller = controller;
        this.navbar = navbar;
        this.properties = properties;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.selectPath("");
    }
    
    
    
    public static GraphPanelContainer create(JsonEditorNavbar navbar, Controller controller, ReadableModel model)
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
        GraphPanelContainer graphView = new GraphPanelContainer(navbar, controller, model, new SmartGraphProperties(propertiesFile), uri);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        VBox.setVgrow(graphView, Priority.ALWAYS);
        return graphView;
        
    }
    
    @Override
    public void updateView()
    {
        this.getChildren().clear();
        this.graphView = new NodeGraphPanel(model, controller, selectedPath, properties, initialPlacement, cssFile);
        this.getChildren().add(graphView);
        Platform.runLater(() -> {
            graphView.init();
            graphView.update();//hacky hack so the labels are properly loaded
        });
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
        // For graph, do complete update for now
        updateView();
    }
    
    // Granular update methods for specific model changes
    public void handlePathAdded(String path)
    {
        // For graph, do complete update for now
        updateView();
    }
    
    public void handlePathRemoved(String path)
    {
        // For graph, do complete update for now
        updateView();
    }
    
    public void handlePathChanged(String path)
    {
        // For graph, do complete update for now
        updateView();
    }
    
    public void handlePathMoved(String path)
    {
        // For graph, do complete update for now
        updateView();
    }
    
    public void handlePathSorted(String path)
    {
        // For graph, do complete update for now
        updateView();
    }
    
    public void handleRemovedSelection(String path)
    {
        if (path.equals(selectedPath))
        {
            selectedPath = null;
        }
        // Do complete update for now
        updateView();
    }
    
    public void handleSettingsChanged()
    {
        // Refresh graph to apply new settings
        updateView();
    }
}
