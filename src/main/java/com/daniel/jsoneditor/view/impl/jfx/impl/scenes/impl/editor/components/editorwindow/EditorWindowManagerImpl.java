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

// manages the positions of editor windows etc
public class EditorWindowManagerImpl implements EditorWindowManager
{
    private static final int MAX_WINDOWS = 7;
    
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
    
    @Override
    public void openPath(String path)
    {
        openPath(path, true);
    }
    
    @Override
    public void openPath(String path, boolean openObjectParentOfArray)
    {
        ObservableList<Node> windowsAsNodes = editorWindowContainer.getItems();
        if (!windowsAsNodes.isEmpty())
        {
            boolean alreadyOpenInWindow = false;
            for (Node windowNode : windowsAsNodes)
            {
                if (windowNode instanceof JsonEditorEditorWindow)
                {
                    JsonEditorEditorWindow window = (JsonEditorEditorWindow) windowNode;
                    if (path.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(path))
                    {
                        alreadyOpenInWindow = true;
                        window.requestFocus();
                        break;
                    }
                }
            }
            if (!alreadyOpenInWindow)
            {
                Node windowAsNode = windowsAsNodes.get(0);
                if (windowAsNode instanceof JsonEditorEditorWindow)
                {
                    JsonEditorEditorWindow firstWindow = (JsonEditorEditorWindow) windowAsNode;
                    firstWindow.setSelectedPath(path);
                }
            }
        }
        else
        {
            // if no window exists, we create a new one and open it in there
            openInNewWindowIfPossible(path, openObjectParentOfArray);
        }
        
    }
    
    @Override
    public void openInNewWindowIfPossible(String path, boolean openObjectParentOfArray)
    {
        if (canAnotherWindowBeAdded())
        {
            addWindow().setSelectedPath(path, openObjectParentOfArray);
        }
        else
        {
            openPath(path, openObjectParentOfArray);
        }
        
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
    public void openInNewWindowIfPossible(String path)
    {
        openInNewWindowIfPossible(path, true);
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
        // if that was not successful, we open the array in another window
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
