package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// manages the positions of editor windows etc. Right now only 1 is supported so nonfunctional
public class EditorWindowManagerImpl implements EditorWindowManager
{
    private static final int MAX_WINDOWS = 3;
    
    private final EditorScene editorScene;
    
    private final SplitPane editorWindowContainer;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    public EditorWindowManagerImpl(EditorScene scene, ReadableModel model, Controller controller)
    {
        this.editorScene = scene;
        this.model = model;
        this.controller = controller;
        editorWindowContainer = makeEditorWindowContainer();
    }
    
    private void addWindow()
    {
        JsonEditorEditorWindow window = new JsonEditorEditorWindow(this, model, controller);
        // add to the right
        editorWindowContainer.getItems().add(window);
    }
    
    @Override
    public SplitPane getEditorWindowContainer()
    {
        return editorWindowContainer;
    }
    
    private SplitPane makeEditorWindowContainer()
    {
        SplitPane pane = new SplitPane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }
    
    @Override
    public void selectFromNavbar(String path)
    {
        ObservableList<Node> windowsAsNodes = editorWindowContainer.getItems();
        if (windowsAsNodes.size() > 0)
        {
            Node firstWindowAsNode = windowsAsNodes.get(0);
            if (firstWindowAsNode instanceof JsonEditorEditorWindow)
            {
                JsonEditorEditorWindow firstWindow = (JsonEditorEditorWindow) firstWindowAsNode;
                firstWindow.setSelectedPath(path);
            }
        }
        else
        {
            // if no window exists, we create a new one and open it in there
            selectInNewWindow(path);
        }
    }
    
    @Override
    public void selectInNewWindow(String path)
    {
        if (canAnotherWindowBeAdded())
        {
            addWindow();
            // we display it in the new rightermost window
            Node rightmostNode = editorWindowContainer.getItems().get(editorWindowContainer.getItems().size() - 1);
            ((JsonEditorEditorWindow) rightmostNode).setSelectedPath(path);
        }
    }
    
    @Override
    public void closeWindow(JsonEditorEditorWindow windowToClose)
    {
        editorWindowContainer.getItems().remove(windowToClose);
    }
    
    @Override
    public void selectOnNavbar(String path)
    {
        editorScene.getNavbar().selectPath(path);
    }
    
    @Override
    public boolean canAnotherWindowBeAdded()
    {
        return editorWindowContainer.getItems().size() < MAX_WINDOWS;
    }
    
    @Override
    public void updateNavbarRepresentation(String path)
    {
        editorScene.getNavbar().updateNavbarItem(path);
    }
    
    @Override
    public void updateEditors()
    {
        for (Node node : editorWindowContainer.getItems())
        {
            JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            window.setSelectedPath(window.getSelectedPath());
        }
    }
}
