package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.scene.paint.Color;


public interface View extends Observer
{
    void cantValidateJson();
    
    void selectJsonAndSchema();
    
    /**
     * Shows a predefined toast from the Toasts enum.
     *
     * @param toast The toast to display
     */
    void showToast(Toasts toast);
    
    /**
     * Shows a custom toast with a message and color.
     *
     * @param message The message to display
     * @param color The color of the toast
     */
    void showCustomToast(String message, Color color);
    
    /**
     * Updates the window title with the given unsaved changes count.
     *
     * @param unsavedChangesCount number of unsaved changes
     */
    void updateWindowTitle(int unsavedChangesCount);

    /**
     * Swaps the model this view observes and re-wires all internal components to use the new model.
     * Called when the controller loads a different file in the same window, replacing the old model
     * with a fresh session-managed instance so that former MCP sessions on the old file are not affected.
     *
     * @param newModel the new model to observe
     */
    void reloadForNewModel(ReadableModel newModel);
}
