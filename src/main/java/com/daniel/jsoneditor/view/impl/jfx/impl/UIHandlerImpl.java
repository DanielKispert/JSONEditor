package com.daniel.jsoneditor.view.impl.jfx.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.impl.EditorDimensions;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.changes.ChangeType;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelectionScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import com.daniel.jsoneditor.view.impl.jfx.toast.impl.ToastImpl;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class UIHandlerImpl implements UIHandler
{
    private final Controller controller;
    
    private final Stage stage;
    
    private final ReadableModel model;
    
    private EditorScene editorScene;
    
    public UIHandlerImpl(Controller controller, Stage stage, ReadableModel model)
    {
        this.controller = controller;
        this.stage = stage;
        this.model = model;
        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            EditorDimensions oldDimensions = controller.getSettingsController().getEditorDimensions();
            controller.getSettingsController().setEditorDimensions(oldDimensions.getWidth(), oldDimensions.getHeight(), newValue);
        });
        
    }
    
    @Override
    public void showSelectJsonAndSchema()
    {
        stage.setScene(new JSONSelectionScene(this, controller, model).getScene(stage));
        stage.setWidth(700);
        stage.setHeight(300);
        stage.show();
    }
    
    @Override
    public void showMainEditor()
    {
        EditorDimensions dimensions = controller.getSettingsController().getEditorDimensions();
        stage.setMaximized(dimensions.isMaximized()); //start maximized if the editor was maximized last
        stage.setWidth(dimensions.getWidth());
        stage.setHeight(dimensions.getHeight());
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - dimensions.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - dimensions.getHeight()) / 2);
        this.editorScene = new EditorScene(this, controller, model);
        stage.setScene(editorScene.getScene(stage));
        stage.show();
    }
    
    @Override
    public void handleAddedReferenceableObject(String pathOfObject)
    {
        if (editorScene != null)
        {
            editorScene.handleAddedReferenceableObject(pathOfObject);
        }
        
    }
    
    @Override
    public void updateEditorSceneWithRemovedJson()
    {
        if (editorScene != null)
        {
            editorScene.updateEverything();
        }
    }
    
    /**
     * Updates the window title to show unsaved changes count.
     *
     * @param unsavedChangesCount number of unsaved changes
     */
    public void updateWindowTitle(final int unsavedChangesCount)
    {
        final String baseTitle = "JSON Editor";
        if (unsavedChangesCount > 0)
        {
            stage.setTitle(baseTitle + " - " + unsavedChangesCount + " unsaved change" +
                          (unsavedChangesCount == 1 ? "" : "s"));
        }
        else
        {
            stage.setTitle(baseTitle);
        }
    }
    
    @Override
    public void updateEditorSceneWithUpdatedStructure()
    {
        if (editorScene != null)
        {
            editorScene.updateEverything();
        }
    }
    
    @Override
    public void updateEditorSceneWithMovedJson()
    {
        if (editorScene != null)
        {
            editorScene.handleMovedSelection();
        }
    }
    
    @Override
    public void showToast(Toasts toast)
    {
        new ToastImpl().show(stage, toast.getMessage(), toast.getColor(), toast.getDuration());
    }
    
    @Override
    public void handleCommandApplied(Event event)
    {
        if (editorScene != null && event.getChanges() != null)
        {
            // Process each model change granularly
            for (final ModelChange change : event.getChanges())
            {
                handleModelChange(change);
            }
        }
    }
    
    /**
     * Handles individual model changes with granular UI updates.
     *
     * @param change the specific model change to handle
     */
    private void handleModelChange(ModelChange change)
    {
        final String path = change.getPath();
        final ChangeType type = change.getType();
        
        switch (type)
        {
            case ADD:
                handleAdd(path);
                break;
            case REMOVE:
                handleRemove(path);
                break;
            case REPLACE:
                handleReplace(path);
                break;
            case MOVE:
                handleMove(path);
                break;
            case SORT:
                handleSort(path);
                break;
            case SETTINGS_CHANGED:
                handleSettingsChange();
                break;
        }
    }
    
    private void handleAdd(String path)
    {
        // Update navbar to show new node
        editorScene.getNavbar().handlePathAdded(path);
        // Update any open editors that might display the parent
        editorScene.getEditorWindowManager().handlePathAdded(path);
        // Select the newly added item
        editorScene.getNavbar().selectPath(path);
    }
    
    private void handleRemove(String path)
    {
        // Update navbar to remove the node
        editorScene.getNavbar().handlePathRemoved(path);
        // Update any open editors that might have displayed this path
        editorScene.getEditorWindowManager().handlePathRemoved(path);
        // Clear selection if removed path was selected
        editorScene.getNavbar().handleRemovedSelection(path);
    }
    
    private void handleReplace(String path)
    {
        // Update navbar display for the changed node
        editorScene.getNavbar().handlePathChanged(path);
        // Update any open editors showing this path
        editorScene.getEditorWindowManager().handlePathChanged(path);
    }
    
    private void handleMove(String path)
    {
        // Update navbar to reflect new order
        editorScene.getNavbar().handlePathMoved(path);
        // Update any open editors showing the parent array
        editorScene.getEditorWindowManager().handlePathMoved(path);
    }
    
    private void handleSort(String path)
    {
        // Update navbar to reflect new sort order
        editorScene.getNavbar().handlePathSorted(path);
        // Update any open editors showing the sorted array
        editorScene.getEditorWindowManager().handlePathSorted(path);
    }
    
    private void handleSettingsChange()
    {
        // Refresh UI elements affected by settings
        editorScene.getNavbar().handleSettingsChanged();
        editorScene.getEditorWindowManager().handleSettingsChanged();
    }
}
