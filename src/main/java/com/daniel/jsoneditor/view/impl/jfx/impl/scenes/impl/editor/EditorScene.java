package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor;

import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.menubar.JsonEditorMenuBar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar.JsonEditorToolbar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
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
    
    private JsonEditorNavbar navbar;
    
    private final EditorWindowManager editorWindowManager;
    
    
    public EditorScene(UIHandler handler, Controller controller, ReadableModel model)
    {
        super(handler, controller, model);
        editorWindowManager = new EditorWindowManagerImpl(this, model, controller);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        double startingSceneWidth = 1400;
        double startingSceneHeight = 800;
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, startingSceneWidth, startingSceneHeight);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        navbar = new JsonEditorNavbar(model, controller, editorWindowManager, stage);
        VBox bars = new VBox(new JsonEditorMenuBar(model, controller, editorWindowManager), new JsonEditorToolbar(model, controller,
                editorWindowManager));
        root.setTop(bars);
        root.setLeft(makeSplitPane(scene));
        return scene;
    }
    
    private SplitPane makeSplitPane(Scene scene)
    {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(navbar, editorWindowManager.getEditorWindowContainer());
        splitPane.setDividerPositions(0.4);
        splitPane.prefWidthProperty().bind(scene.widthProperty());
        splitPane.prefHeightProperty().bind(scene.heightProperty());
        return splitPane;
    }
    
    public void handleAddedArrayItem(String pathOfAddedArrayItem)
    {
        editorWindowManager.updateEditors();
        navbar.updateTree();
        // desired navbar behavior: select the item that we just added if it's not already selected
        navbar.selectPath(pathOfAddedArrayItem);
        // desired editor behavior: if array is already open, scroll to the added item. If no array is open, open an array and scroll
        editorWindowManager.focusOnArrayItem(pathOfAddedArrayItem);
    }
    
    
    public void updateEverything()
    {
        // we also update the windows because they could show the parent array, which just had something added/removed/changed
        editorWindowManager.updateEditors();
        navbar.updateTree();
    }
    
    public void handleMovedSelection()
    {
        navbar.updateTree();
    }
    
    public JsonEditorNavbar getNavbar()
    {
        return navbar;
    }
    
    

    
}
