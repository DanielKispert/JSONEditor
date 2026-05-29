package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.AutoAdjustingSplitPane;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.stage.Screen;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map;

// manages the positions of editor windows etc
public class EditorWindowManagerImpl implements EditorWindowManager
{
    private final EditorScene editorScene;
    
    private final SplitPane editorWindowContainer;
    
    private final Controller controller;
    
    private final ReadableModel model;
    
    private final SettingsController settingsController;
    
    public EditorWindowManagerImpl(EditorScene scene, ReadableModel model, Controller controller)
    {
        this.editorScene = scene;
        this.model = model;
        this.controller = controller;
        this.settingsController = controller.getSettingsController();
        editorWindowContainer = new AutoAdjustingSplitPane();
    }
    
    /**
     * Returns the effective maximum number of editor windows. If the setting is "auto", the limit is derived from the primary screen
     * width: roughly 1 window per 350px, clamped between 3 and 10.
     */
    private int getMaxWindows()
    {
        String setting = settingsController.getMaxEditorWindows();
        if (!"auto".equals(setting))
        {
            try
            {
                return Math.max(3, Math.min(10, Integer.parseInt(setting)));
            }
            catch (NumberFormatException ignored)
            {
                // fall through to auto
            }
        }
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        int computed = (int) (screenWidth / 350.0);
        return Math.max(3, Math.min(10, computed));
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
            // 1. Check if the path is already visible in an existing window
            if (focusExistingWindowForPath(path))
            {
                return;
            }
            // 2. Path is not open anywhere — prefer opening in a new window if possible
            if (canAnotherWindowBeAdded())
            {
                addWindow().setSelectedPath(path, openObjectParentOfArray);
            }
            else
            {
                // Fall back to reusing the first window
                Node windowAsNode = windowsAsNodes.get(0);
                if (windowAsNode instanceof JsonEditorEditorWindow)
                {
                    JsonEditorEditorWindow firstWindow = (JsonEditorEditorWindow) windowAsNode;
                    firstWindow.setSelectedPath(path, openObjectParentOfArray);
                }
            }
        }
        else
        {
            // if no window exists, we create a new one and open it in there
            openInNewWindowIfPossible(path, openObjectParentOfArray);
        }
    }
    
    /**
     * Checks if the given path is already visible in any editor window (as main selection or child table).
     * If found, flashes the window and returns true.
     */
    private boolean focusExistingWindowForPath(String path)
    {
        final ObservableList<Node> windowsAsNodes = editorWindowContainer.getItems();
        for (final Node windowNode : windowsAsNodes)
        {
            if (windowNode instanceof JsonEditorEditorWindow)
            {
                final JsonEditorEditorWindow window = (JsonEditorEditorWindow) windowNode;
                if (path.equals(window.getSelectedPath()))
                {
                    window.flash();
                    return true;
                }
                if (window.getOpenChildPaths().contains(path))
                {
                    window.flash();
                    window.focusArrayItem(path);
                    return true;
                }
            }
        }

        // nuanced parent-path fallback: only when the node would be diverted to its parent array
        final String parentPath = PathHelper.getParentPath(path);
        if (parentPath != null && wouldDivertToParentArray(path, parentPath))
        {
            for (final Node windowNode : windowsAsNodes)
            {
                if (windowNode instanceof JsonEditorEditorWindow)
                {
                    final JsonEditorEditorWindow window = (JsonEditorEditorWindow) windowNode;
                    if (parentPath.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(parentPath))
                    {
                        window.flash();
                        window.focusArrayItem(path);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns true if navigating to {@code path} would be diverted back to {@code parentPath} by
     * {@link JsonEditorEditorWindow#divertPathToSelect}: specifically, when the parent is an array
     * and the child is either a primitive or a flat object (no nested object/array fields).
     * Returns false when the model node is unknown (null or missing) — safe default that prevents
     * spurious parent flashes.
     */
    private boolean wouldDivertToParentArray(String path, String parentPath)
    {
        final JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode == null || !parentNode.isArray())
        {
            return false;
        }
        final JsonNodeWithPath node = model.getNodeForPath(path);
        if (node == null || node.isMissing())
        {
            return false;
        }
        if (!node.isObject())
        {
            return true;  // primitive → always diverted to parent array
        }
        // Object: diverted only when it has no nested object/array fields
        final Iterator<Map.Entry<String, JsonNode>> fields = node.getNode().fields();
        while (fields.hasNext())
        {
            final Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getValue().isObject() || entry.getValue().isArray())
            {
                return false;
            }
        }
        return true;  // flat object → diverted to parent array
    }
    
    @Override
    public void openInNewWindowIfPossible(String path, boolean openObjectParentOfArray)
    {
        // First check if the path is already visible in an existing window — no need to open a duplicate
        if (focusExistingWindowForPath(path))
        {
            return;
        }
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
        return editorWindowContainer.getItems().size() < getMaxWindows();
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
    
    @Override
    public void handlePathAdded(String path)
    {
        // Update any windows that might be displaying the parent of the added path
        final String parentPath = PathHelper.getParentPath(path);
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            if (parentPath.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(parentPath))
            {
                window.handleChildAdded(path);
            }
        }
    }
    
    @Override
    public void handlePathRemoved(String path)
    {
        // Close any windows showing the removed path
        editorWindowContainer.getItems().removeIf(node -> {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            return path.equals(window.getSelectedPath());
        });
        
        // Update any windows that might be displaying the parent of the removed path
        final String parentPath = PathHelper.getParentPath(path);
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            if (parentPath.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(parentPath))
            {
                window.handleChildRemoved(path);
            }
        }
    }
    
    @Override
    public void handlePathChanged(String path)
    {
        // Update any windows showing this specific path or its children
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            if (path.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(path) || path.startsWith(window.getSelectedPath()))
            {
                window.handlePathChanged(path);
            }
        }
    }
    
    @Override
    public void handlePathMoved(ModelChange change)
    {
        final String path = change.getPath();
        // Update any windows showing the parent array that contains the moved item
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            if (path.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(path))
            {
                window.handleChildMoved(change);
            }
        }
    }
    
    @Override
    public void handlePathSorted(String path)
    {
        // Update any windows showing the sorted array
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            if (path.equals(window.getSelectedPath()) || window.getOpenChildPaths().contains(path))
            {
                window.handleSorted(path);
            }
        }
    }
    
    @Override
    public void handleSettingsChanged()
    {
        // Refresh all windows to apply new settings
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            window.handleSettingsChanged();
        }
    }
    
    @Override
    public void handleGitBlameLoaded()
    {
        for (Node node : editorWindowContainer.getItems())
        {
            final JsonEditorEditorWindow window = (JsonEditorEditorWindow) node;
            window.handleGitBlameLoaded();
        }
    }
}
