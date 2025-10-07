package com.daniel.jsoneditor.controller.impl.commands;

import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.Command;

import java.util.List;

/**
 * Interface for managing command execution history and undo/redo operations.
 */
public interface CommandManager
{
    /**
     * Executes a command and records its resulting changes on the undo stack.
     *
     * @param cmd command to execute
     * @return list of produced changes (empty if no state mutation)
     */
    List<ModelChange> executeCommand(Command cmd);
    
    /**
     * Reverts the last executed command if present.
     *
     * @return original ModelChange list of undone command (empty if nothing to undo)
     */
    List<ModelChange> undo();
    
    /**
     * Re-applies the last undone command if present.
     *
     * @return list of re-applied original changes (empty if nothing to redo)
     */
    List<ModelChange> redo();
    
    /**
     * Clears both undo and redo stacks. Used when resetting the root node to prevent inconsistent state.
     */
    void clearHistory();
    
    /**
     * Marks the current state as saved, resetting the unsaved changes counter.
     */
    void markAsSaved();
    
    /**
     * Sets the callback to be notified when unsaved changes count changes.
     *
     * @param callback callback to notify about unsaved changes count updates
     */
    void setUnsavedChangesCallback(UnsavedChangesCallback callback);
    
    /**
     * Callback interface for unsaved changes notifications.
     */
    @FunctionalInterface
    interface UnsavedChangesCallback
    {
        /**
         * Called whenever the unsaved changes count changes.
         *
         * @param unsavedChangesCount new count of unsaved changes
         */
        void onUnsavedChangesCountChanged(int unsavedChangesCount);
    }
}
