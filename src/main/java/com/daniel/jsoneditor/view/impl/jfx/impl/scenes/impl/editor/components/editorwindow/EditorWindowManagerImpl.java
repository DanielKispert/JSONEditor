package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// manages the positions of editor windows etc. Right now only 1 is supported so nonfunctional
public class EditorWindowManagerImpl implements EditorWindowManager
{
    
    private final EditorScene editorScene;
    
    private List<JsonEditorEditorWindow> windows;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    public EditorWindowManagerImpl(EditorScene scene, ReadableModel model, Controller controller)
    {
        this.editorScene = scene;
        this.windows = new ArrayList<>();
        this.model = model;
        this.controller = controller;
        windows.add(new JsonEditorEditorWindow(this, model, controller));
    }
    
    @Override
    public HBox getEditorWindows()
    {
        HBox hBox = new HBox();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        VBox.setVgrow(hBox, Priority.ALWAYS);
        hBox.getChildren().addAll(windows);
        return hBox;
    }
    
    @Override
    public void selectFromNavbar(String path)
    {
        // for now, we only support one window, so we simply tell the one window to select that path
        windows.get(0).setSelectedPath(path);
    }
    
    @Override
    public void selectOnNavbar(String path)
    {
        editorScene.getNavbar().selectPath(path);
    }
    
    @Override
    public void updateNavbarRepresentation(String path)
    {
        editorScene.getNavbar().updateNavbarItem(path);
    }
    
    @Override
    public void updateEditors()
    {
        for (JsonEditorEditorWindow window : windows)
        {
            window.setSelectedPath(window.getSelectedPath());
        }
    }
}
