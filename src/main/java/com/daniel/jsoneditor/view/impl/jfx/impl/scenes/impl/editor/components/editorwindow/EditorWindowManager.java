package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import javafx.scene.control.SplitPane;

public interface EditorWindowManager
{
    SplitPane getEditorWindowContainer();
    
    /**
     * a path gets selected from the navbar and has to be displayed in an editor window (we'll decide which one)
     */
    void selectFromNavbar(String path);
    
    void selectInNewWindow(String path);
    
    void closeWindow(JsonEditorEditorWindow windowToClose);
    
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
}
