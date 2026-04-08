package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import java.util.List;
import java.util.function.Consumer;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.buttons.DiffButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.HistoryButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.NavBarSwitchButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.graph.GraphPanelContainer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Container with a button bar at the top and a switchable view (tree or graph) below.
 */
public class JsonEditorNavbar extends VBox
{
    private final EditorNavTree navTreeView;
    
    private final GraphPanelContainer graphView;
    
    private final List<NavbarElement> elements;
    
    public JsonEditorNavbar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager, Stage stage)
    {
        navTreeView = new EditorNavTree(this, model, controller, editorWindowManager, stage);
        graphView = GraphPanelContainer.create(controller, model);
        elements = List.of(navTreeView, graphView);
        
        final HBox buttonBar = makeButtonBar(model, controller, editorWindowManager, stage);
        final StackPane windowContainer = new StackPane(graphView, navTreeView);
        VBox.setVgrow(windowContainer, Priority.ALWAYS);
        
        getChildren().addAll(buttonBar, windowContainer);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }
    
    private HBox makeButtonBar(ReadableModel model, Controller controller, EditorWindowManager editorWindowManager, Stage stage)
    {
        final HBox buttonBar = new HBox();
        final DiffButton diffButton = new DiffButton(controller, editorWindowManager, stage);
        final HistoryButton historyButton = new HistoryButton(controller, stage);
        final NavBarSwitchButton navBarSwitchButton = new NavBarSwitchButton(model, this);
        buttonBar.getChildren().addAll(diffButton, historyButton, navBarSwitchButton);
        HBox.setHgrow(buttonBar, Priority.NEVER);
        return buttonBar;
    }
    
    public void selectPath(String path)
    {
        forEachElement(e -> e.selectPath(path));
    }
    
    public void handleUpdate()
    {
        forEachElement(NavbarElement::updateView);
    }
    
    public void updateNavbarItem(String path)
    {
        navTreeView.updateSingleElement(path);
    }
    
    public void showNavTreeView()
    {
        navTreeView.setVisible(true);
        navTreeView.toFront();
        graphView.setVisible(false);
    }
    
    public void showGraphView()
    {
        graphView.setVisible(true);
        graphView.toFront();
        navTreeView.setVisible(false);
    }
    
    public void handlePathAdded(String path)
    {
        forEachElement(e -> e.handlePathAdded(path));
    }
    
    public void handlePathRemoved(String path)
    {
        forEachElement(e -> e.handlePathRemoved(path));
    }
    
    public void handlePathChanged(String path)
    {
        forEachElement(e -> e.handlePathChanged(path));
    }
    
    public void handlePathMoved(String path)
    {
        forEachElement(e -> e.handlePathMoved(path));
    }
    
    public void handlePathSorted(String path)
    {
        forEachElement(e -> e.handlePathSorted(path));
    }
    
    public void handleRemovedSelection(String path)
    {
        forEachElement(e -> e.handleRemovedSelection(path));
    }
    
    public void handleSettingsChanged()
    {
        forEachElement(NavbarElement::handleSettingsChanged);
    }
    
    private void forEachElement(Consumer<NavbarElement> action)
    {
        elements.forEach(action);
    }
}
