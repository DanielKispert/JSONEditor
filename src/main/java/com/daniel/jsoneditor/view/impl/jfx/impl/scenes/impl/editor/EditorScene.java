package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor;

import com.daniel.jsoneditor.controller.settings.impl.EditorDimensions;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ToggleSidebarButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.menubar.JsonEditorMenuBar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar.JsonEditorToolbar;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.SceneHandlerImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManagerImpl;

public class EditorScene extends SceneHandlerImpl
{
    private static final double DEFAULT_DIVIDER_POSITION = 0.2;
    
    private JsonEditorNavbar navbar;
    
    private final EditorWindowManager editorWindowManager;
    
    private SplitPane splitPane;
    
    private double lastDividerPosition = DEFAULT_DIVIDER_POSITION;
    
    private boolean navbarCollapsed;
    
    private ToggleSidebarButton toggleSidebarButton;
    
    
    public EditorScene(UIHandler handler, Controller controller, ReadableModel model)
    {
        super(handler, controller, model);
        editorWindowManager = new EditorWindowManagerImpl(this, model, controller);
        navbarCollapsed = controller.getSettingsController().isNavbarCollapsed();
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        navbar = new JsonEditorNavbar(model, controller, editorWindowManager, stage);
        toggleSidebarButton = new ToggleSidebarButton(this::toggleNavbar, this::isNavbarCollapsed);
        VBox bars = new VBox(new JsonEditorMenuBar(model, controller, editorWindowManager, navbar, this::toggleNavbar),
                new JsonEditorToolbar(model, controller, editorWindowManager, navbar, toggleSidebarButton));
        root.setTop(bars);
        splitPane = makeSplitPane();
        root.setCenter(splitPane);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            EditorDimensions oldDimensions = controller.getSettingsController().getEditorDimensions();
            //resizing the window means its no longer maximized
            controller.getSettingsController().setEditorDimensions(newValue.intValue(), oldDimensions.getHeight(), false);
        });
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            EditorDimensions oldDimensions = controller.getSettingsController().getEditorDimensions();
            //resizing the window means its no longer maximized
            controller.getSettingsController().setEditorDimensions(oldDimensions.getWidth(), newValue.intValue(), false);
        });
        return scene;
    }
    
    private SplitPane makeSplitPane()
    {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        if (navbarCollapsed)
        {
            splitPane.getItems().add(editorWindowManager.getEditorWindowContainer());
        }
        else
        {
            splitPane.getItems().addAll(navbar, editorWindowManager.getEditorWindowContainer());
            Platform.runLater(() -> splitPane.setDividerPositions(lastDividerPosition));
        }
        return splitPane;
    }
    
    private void toggleNavbar()
    {
        if (navbarCollapsed)
        {
            expandNavbar();
        }
        else
        {
            collapseNavbar();
        }
        toggleSidebarButton.updateAppearance();
    }
    
    private void collapseNavbar()
    {
        if (splitPane.getDividerPositions().length > 0)
        {
            lastDividerPosition = splitPane.getDividerPositions()[0];
        }
        splitPane.getItems().remove(navbar);
        navbarCollapsed = true;
        controller.getSettingsController().setNavbarCollapsed(true);
    }
    
    private void expandNavbar()
    {
        if (!splitPane.getItems().contains(navbar))
        {
            splitPane.getItems().add(0, navbar);
            Platform.runLater(() -> splitPane.setDividerPositions(lastDividerPosition));
        }
        navbarCollapsed = false;
        controller.getSettingsController().setNavbarCollapsed(false);
    }
    
    private boolean isNavbarCollapsed()
    {
        return navbarCollapsed;
    }
    
    public void updateEverything()
    {
        // we also update the windows because they could show the parent array, which just had something added/removed/changed
        editorWindowManager.updateEditors();
        navbar.handleUpdate();
    }
    
    public void handleMovedSelection()
    {
        navbar.handleUpdate();
        editorWindowManager.updateEditors();
    }
    
    public JsonEditorNavbar getNavbar()
    {
        return navbar;
    }
    
    public EditorWindowManager getEditorWindowManager()
    {
        return editorWindowManager;
    }
}
