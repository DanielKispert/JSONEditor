package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.NavBarSwitchButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph.NodeGraphPanel;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * object with a button bar at the top and a window at the bottom for either seeing a graph or a navtree
 */
public class JsonEditorNavbar extends VBox
{
    private final ReadableModel model;
    
    private final HBox buttonBar;
    
    private final StackPane windowContainer;
    
    private final EditorNavTree navTreeView;
    
    private final NodeGraphPanel graphView;
    
    
    public JsonEditorNavbar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager, Stage stage)
    {
        this.model = model;
        buttonBar = makeButtonBar();
        navTreeView = new EditorNavTree(model, controller, editorWindowManager, stage);
        graphView = NodeGraphPanel.create(model);
        windowContainer = makeWindowContainer();
        getChildren().addAll(buttonBar, windowContainer);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }
    
    private HBox makeButtonBar()
    {
        HBox buttonBar = new HBox();
        NavBarSwitchButton navBarSwitchButton = new NavBarSwitchButton(model, this);
        buttonBar.getChildren().add(navBarSwitchButton);
        HBox.setHgrow(buttonBar, Priority.NEVER);
        return buttonBar;
    }
    
    private StackPane makeWindowContainer()
    {
        StackPane windowContainer = new StackPane();
        windowContainer.getChildren().addAll(graphView, navTreeView);
        Platform.runLater(graphView::init);
        VBox.setVgrow(windowContainer, Priority.ALWAYS);
        return windowContainer;
    }
    
    public void selectPath(String path)
    {
        navTreeView.selectPath(path);
    }
    
    public void handleUpdate()
    {
        navTreeView.updateTree();
    }
    
    public void updateNavbarItem(String path)
    {
        navTreeView.updateNavbarItem(path);
    }
    
    public void showNavTreeView()
    {
        navTreeView.toFront();
    }
    
    public void showGraphView()
    {
        graphView.toFront();
    }
    
}
