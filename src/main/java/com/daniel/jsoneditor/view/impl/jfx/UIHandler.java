package com.daniel.jsoneditor.view.impl.jfx;

import javax.annotation.processing.SupportedAnnotationTypes;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;


public interface UIHandler
{
    
    void showSelectJsonAndSchema();
    
    void showMainEditor();
    
    void handleAddedReferenceableObject(String pathOfObject);
    
    void handleAddedArrayItem(String pathOfArrayItem);
    
    void updateEditorSceneWithSelectedJson();
    
    void updateEditorSceneWithRemovedJson();
    
    void updateEditorSceneWithUpdatedStructure();
    
    void updateEditorSceneWithMovedJson();
    
    void showToast(Toasts toast);
    
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
    
}
