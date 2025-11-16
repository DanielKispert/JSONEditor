package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.GraphFilterButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.GraphInfoButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.NavbarElement;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    
    private GraphFilterButton filterButton;
    
    private Set<String> currentFilteredEdgeNames = null; // null means show all edges
    
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
        this.graphView = new NodeGraphPanel(model, controller, selectedPath, properties, initialPlacement, cssFile, currentFilteredEdgeNames);
        this.graphView.setFilterUpdateCallback(this::handleNewEdgeNames);

        final VBox mainContainer = new VBox();
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        HBox.setHgrow(mainContainer, Priority.ALWAYS);
        
        final HBox buttonsContainer = new HBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(8));
        
        if (this.filterButton == null)
        {
            this.filterButton = new GraphFilterButton(this::getAvailableEdgeNames, this::applyEdgeFilter);
        }
        
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        final GraphInfoButton infoButton = new GraphInfoButton();
        
        buttonsContainer.getChildren().addAll(filterButton, spacer, infoButton);
        
        final StackPane graphContainer = new StackPane();
        StackPane.setAlignment(graphView, Pos.CENTER);
        graphContainer.getChildren().add(graphView);
        VBox.setVgrow(graphContainer, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(buttonsContainer, graphContainer);
        
        HBox.setHgrow(mainContainer, Priority.ALWAYS);
        this.getChildren().add(mainContainer);
        
        Platform.runLater(() -> {
            graphView.init();
            graphView.update();
        });
    }
    
    private Collection<String> getAvailableEdgeNames()
    {
        if (graphView != null)
        {
            return graphView.getAllEdgeNames();
        }
        return java.util.Collections.emptyList();
    }
    
    private void applyEdgeFilter()
    {
        if (filterButton != null)
        {
            final List<String> selectedEdgeNames = filterButton.getSelectedEdgeNames();
            
            if (selectedEdgeNames == null)
            {
                currentFilteredEdgeNames = java.util.Collections.emptySet();
            }
            else if (selectedEdgeNames.isEmpty())
            {
                currentFilteredEdgeNames = null;
            }
            else
            {
                currentFilteredEdgeNames = new java.util.HashSet<>(selectedEdgeNames);
            }
            
            updateView();
        }
    }
    
    private void handleNewEdgeNames()
    {
        if (filterButton != null)
        {
            Collection<String> allEdgeNames = getAvailableEdgeNames();
            filterButton.addNewItemsAsSelected(allEdgeNames);
        }
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
        updateView();
    }
    
    public void handlePathAdded(String path)
    {
        updateView();
    }
    
    public void handlePathRemoved(String path)
    {
        updateView();
    }
    
    public void handlePathChanged(String path)
    {
        updateView();
    }
    
    public void handlePathMoved(String path)
    {
        updateView();
    }
    
    public void handlePathSorted(String path)
    {
        updateView();
    }
    
    public void handleRemovedSelection(String path)
    {
        if (path.equals(selectedPath))
        {
            selectedPath = null;
        }
        updateView();
    }
    
    public void handleSettingsChanged()
    {
        updateView();
    }
}
