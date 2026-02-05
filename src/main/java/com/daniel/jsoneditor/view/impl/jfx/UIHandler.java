package com.daniel.jsoneditor.view.impl.jfx;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.view.impl.jfx.toast.ToastLike;
import javafx.scene.paint.Color;


public interface UIHandler
{
    
    void showSelectJsonAndSchema();
    
    void showMainEditor();
    
    void updateEditorSceneWithRemovedJson();
    
    void updateEditorSceneWithUpdatedStructure();
    
    void updateEditorSceneWithMovedJson();
    

    void showToast(ToastLike toast);
    
    /**
     * Shows a toast with a custom message and color.
     *
     * @param message The message to display
     * @param color The color of the toast
     */
    void showToastMessage(String message, Color color);
    
    /**
     * Updates the window title with the given unsaved changes count.
     *
     * @param unsavedChangesCount number of unsaved changes
     */
    void updateWindowTitle(int unsavedChangesCount);
    
    /**
     * Handles command execution events (execute/undo/redo)
     * @param event event containing command metadata and changes
     */
    void handleCommandApplied(Event event);
    
    /**
     * Handles git blame data being loaded, refreshes relevant UI components.
     */
    void handleGitBlameLoaded();
    
}
