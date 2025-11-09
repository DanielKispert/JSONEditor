package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.GraphInfoButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.NavbarElement;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class GraphPanelContainer extends HBox implements NavbarElement
{
    private static final String PATH_TO_PROPERTIES = "brunomnsilva/smartgraph/smartgraph.properties";
    
    private static final String PATH_TO_CSS = "brunomnsilva/smartgraph/smartgraph.css";
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    private NodeGraphPanel graphView;
    
    private final SmartGraphProperties properties;
    
    private final SmartPlacementStrategy initialPlacement;
    
    private final URI cssFile;
    
    private String selectedPath;
    
    private GraphPanelContainer(Controller controller, ReadableModel model, SmartGraphProperties properties, URI cssFile)
    {
        this.initialPlacement = new JsonPlacementStrategy();
        this.cssFile = cssFile;
        this.controller = controller;
        this.properties = properties;
        this.model = model;
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        this.selectPath("");
    }
    
    
    
    public static GraphPanelContainer create(Controller controller, ReadableModel model)
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
        GraphPanelContainer graphView = new GraphPanelContainer(controller, model, new SmartGraphProperties(propertiesFile), uri);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        VBox.setVgrow(graphView, Priority.ALWAYS);
        return graphView;
        
    }
    
    @Override
    public void updateView()
    {
        this.getChildren().clear();
        this.graphView = new NodeGraphPanel(model, controller, selectedPath, properties, initialPlacement, cssFile);

        final StackPane stack = new StackPane();
        StackPane.setAlignment(graphView, Pos.CENTER);
        stack.getChildren().add(graphView);

        final Button infoButton = new GraphInfoButton();
        StackPane.setAlignment(infoButton, Pos.TOP_RIGHT);
        StackPane.setMargin(infoButton, new Insets(8));
        stack.getChildren().add(infoButton);

        HBox.setHgrow(stack, Priority.ALWAYS);
        this.getChildren().add(stack);

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
