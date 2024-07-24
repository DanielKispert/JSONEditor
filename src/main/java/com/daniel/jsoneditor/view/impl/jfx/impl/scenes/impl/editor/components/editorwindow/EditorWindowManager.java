package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import javafx.scene.control.SplitPane;

public interface EditorWindowManager
{
    SplitPane getEditorWindowContainer();
    
    void openPath(String path);
    
    void openInNewWindowIfPossible(String path);
    
    void closeWindow(JsonEditorEditorWindow windowToClose);
    
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
}
