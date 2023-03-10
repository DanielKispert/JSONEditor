package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.JsonEditorToolbar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.SceneHandlerImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManagerImpl;

public class EditorScene extends SceneHandlerImpl
{
    
    private JsonEditorNavbar navbar;
    
    private JsonEditorToolbar toolbar;
    
    private final EditorWindowManager editorWindowManager;
    
    
    public EditorScene(Controller controller, ReadableModel model)
    {
        super(controller, model);
        editorWindowManager = new EditorWindowManagerImpl(model, controller);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        double startingSceneWidth = 800;
        double startingSceneHeight = 600;
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, startingSceneWidth, startingSceneHeight);
        
        toolbar = new JsonEditorToolbar(model, controller, editorWindowManager);
        navbar = new JsonEditorNavbar(model, controller, editorWindowManager);
        root.setTop(toolbar);
        root.setLeft(makeSplitPane(scene));
        return scene;
    }
    
    private SplitPane makeSplitPane(Scene scene)
    {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(navbar, editorWindowManager.getEditorWindows());
        splitPane.setDividerPositions(0.4);
        splitPane.prefWidthProperty().bind(scene.widthProperty());
        splitPane.prefHeightProperty().bind(scene.heightProperty());
        return splitPane;
    }
    
    public void handleUpdatedSelection()
    {
        editorWindowManager.updateEditors();
    }
    
    public void handleRemovedSelection()
    {
        editorWindowManager.updateEditors();
        navbar.updateTree();
    }
    
    public void handleMovedSelection()
    {
        navbar.updateTree();
    }
    
    

    
}
