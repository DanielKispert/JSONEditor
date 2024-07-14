package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.AutoAdjustingSplitPane;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
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
    private static final int MAX_WINDOWS = 5;
    
    private final EditorScene editorScene;
    
    private final SplitPane editorWindowContainer;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    public EditorWindowManagerImpl(EditorScene scene, ReadableModel model, Controller controller)
    {
        this.editorScene = scene;
        this.model = model;
        this.controller = controller;
        editorWindowContainer = new AutoAdjustingSplitPane();
    }
    
    private JsonEditorEditorWindow addWindow()
    {
        JsonEditorEditorWindow window = new JsonEditorEditorWindow(this, model, controller);
        // add to the right
        editorWindowContainer.getItems().add(window);
        return window;
    }
    
    @Override
    public SplitPane getEditorWindowContainer()
    {
        return editorWindowContainer;
    }
    
    @Override
    public void selectInFirstWindow(String path)
    {
        ObservableList<Node> windowsAsNodes = editorWindowContainer.getItems();
        if (!windowsAsNodes.isEmpty())
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
            addWindow().setSelectedPath(path);
        }
    }
    
    @Override
    public void focusOnArrayItem(String pathOfArrayItem)
    {
        String parentPath = PathHelper.getParentPath(pathOfArrayItem);
        if (parentPath == null)
        {
            return;
        }
        // first we check if a window already has the array open (or the parent object)
        boolean atLeastOneArrayInWindow = false;
        for (Node windowNode : editorWindowContainer.getItems())
        {
            if (windowNode instanceof JsonEditorEditorWindow)
            {
                JsonEditorEditorWindow window = (JsonEditorEditorWindow) windowNode;
                if (parentPath.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(parentPath))
                {
                    atLeastOneArrayInWindow = true;
                    window.focusArrayItem(pathOfArrayItem);
                }
            }
        }
        // if that was not successful, we open it in another window
        if (!atLeastOneArrayInWindow && canAnotherWindowBeAdded())
        {
            JsonEditorEditorWindow newWindow = addWindow();
            newWindow.setSelectedPath(parentPath);
            newWindow.focusArrayItem(pathOfArrayItem);
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
    
    @Override
    public void showToast(Toasts toast)
    {
        editorScene.getHandlerForToasting().showToast(toast);
    }
}
