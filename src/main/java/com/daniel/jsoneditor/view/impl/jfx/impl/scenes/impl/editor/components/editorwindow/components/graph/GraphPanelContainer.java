package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.buttons.GraphFilterButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.GraphInfoButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.NavbarElement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
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
    
    private final TreeSet<String> allKnownEdgeNames = new TreeSet<>();
    
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
        this.graphView = new NodeGraphPanel(model, controller.getSettingsController(), selectedPath, properties, initialPlacement, cssFile,
                currentFilteredEdgeNames);
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
        
        final Button resetButton = new Button();
        ButtonHelper.setButtonImage(resetButton, "/icons/material/darkmode/outline_cluster_white_24dp.png");
        resetButton.setTooltip(new Tooltip("Re-cluster all dissolved clusters"));
        resetButton.setOnAction(e -> updateView());
        
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        final GraphInfoButton infoButton = new GraphInfoButton();
        
        buttonsContainer.getChildren().addAll(filterButton, resetButton, spacer, infoButton);
        
        final StackPane graphContainer = new StackPane();
        StackPane.setAlignment(graphView, Pos.CENTER);
        graphContainer.getChildren().add(graphView);
        VBox.setVgrow(graphContainer, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(buttonsContainer, graphContainer);
        
        HBox.setHgrow(mainContainer, Priority.ALWAYS);
        this.getChildren().add(mainContainer);
        
        // init() requires non-zero dimensions; defer until layout pass completes
        // Must listen on BOTH dimensions — whichever fires first may find the other still 0
        final ChangeListener<Number> initListener = new ChangeListener<>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal)
            {
                if (graphView.getWidth() > 0 && graphView.getHeight() > 0)
                {
                    graphView.widthProperty().removeListener(this);
                    graphView.heightProperty().removeListener(this);
                    graphView.init();
                    graphView.update();
                }
            }
        };
        graphView.widthProperty().addListener(initListener);
        graphView.heightProperty().addListener(initListener);
    }
    
    private Collection<String> getAvailableEdgeNames()
    {
        return new ArrayList<>(allKnownEdgeNames);
    }
    
    private void applyEdgeFilter()
    {
        if (filterButton != null)
        {
            final List<String> selectedEdgeNames = filterButton.getSelectedEdgeNames();
            
            if (selectedEdgeNames == null)
            {
                currentFilteredEdgeNames = Collections.emptySet();
            }
            else if (selectedEdgeNames.isEmpty())
            {
                currentFilteredEdgeNames = null;
            }
            else
            {
                currentFilteredEdgeNames = new HashSet<>(selectedEdgeNames);
            }
            
            updateView();
        }
    }
    
    private void handleNewEdgeNames(Collection<String> newEdgeNames)
    {
        allKnownEdgeNames.addAll(newEdgeNames);
        if (filterButton != null)
        {
            if (currentFilteredEdgeNames == null)
            {
                // "show all" mode - new edges are selected by default
                filterButton.addNewItemsAsSelected(newEdgeNames);
            }
            else
            {
                // Filtered mode - new edges are added as deselected options
                filterButton.addNewItemsAsDeselected(newEdgeNames);
            }
        }
    }

    @Override
    public void selectPath(String path)
    {
        if (!Objects.equals(selectedPath, path))
        {
            selectedPath = path;
            currentFilteredEdgeNames = null;
            filterButton = null;
            allKnownEdgeNames.clear();
            // Collect all edge names from the unfiltered graph so the filter popup always shows the complete list
            model.getJsonAsGraph(selectedPath, null).edges()
                    .forEach(edge -> allKnownEdgeNames.add(edge.element().getName()));
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
