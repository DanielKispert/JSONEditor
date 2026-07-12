package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.scene.control.SplitPane;

public interface EditorWindowManager
{
    SplitPane getEditorWindowContainer();
    

    void openPath(String path);
    
    /**
     * opens the path in an existing window if possible, otherwise opens it in the leftmost window
     */
    void openPath(String path, boolean openObjectParentOfArray);
    
    void openInNewWindowIfPossible(String path);
    
    /**
     * opens the path in a new window, otherwise opens it in an existing window using openPath
     * @param openObjectParentOfArray if true and the path points to an array, then the parent object of the array will be opened
     * instead, and the array will be shown in a child view
     */
    void openInNewWindowIfPossible(String path, boolean openObjectParentOfArray);
    
    void closeWindow(JsonEditorEditorWindow windowToClose);
    
    /**
     * Closes all editor windows except the given one.
     */
    void closeOtherWindows(JsonEditorEditorWindow windowToKeep);
    
    /**
     * Closes all editor windows.
     */
    void closeAllWindows();
    
    /**
     * Closes all editor windows positioned to the right of the reference window in the SplitPane items list.
     * Does nothing if the reference window is not present.
     */
    void closeWindowsToTheRight(JsonEditorEditorWindow reference);
    
    /**
     * Closes all editor windows positioned to the left of the reference window in the SplitPane items list.
     * Does nothing if the reference window is not present.
     */
    void closeWindowsToTheLeft(JsonEditorEditorWindow reference);
    
    /**
     * Returns the number of open editor windows.
     */
    int getOpenWindowCount();

    /**
     * Returns true if at least one editor window exists after the reference window in the SplitPane order.
     * Returns false if the reference window is not present in the list.
     */
    boolean hasWindowsToTheRight(JsonEditorEditorWindow reference);

    /**
     * Returns true if at least one editor window exists before the reference window in the SplitPane order.
     * Returns false if the reference window is not present in the list.
     */
    boolean hasWindowsToTheLeft(JsonEditorEditorWindow reference);
    
    /**
     * focuses the windows with the parent array onto this item. If no window has the array open, opens a new window with the array and
     * scrolls to the item
     */
    void focusOnArrayItem(String pathOfArrayItem);
    
    /**
     * an editor window requests the navbar to select an item
     */
    void selectOnNavbar(String path);
    
    boolean canAnotherWindowBeAdded();
    
    /**
     * the navbar item at this path gets updated
     */
    void updateNavbarRepresentation(String path);
    
    void updateEditors();
    
    void showToast(Toasts toast);
    
    // Granular update methods for specific model changes
    void handlePathAdded(String path);
    
    void handlePathRemoved(String path);
    
    void handlePathChanged(String path);
    
    void handlePathMoved(ModelChange change);
    
    void handlePathSorted(String path);
    
    void handleSettingsChanged();
    
    void handleGitBlameLoaded();
}
