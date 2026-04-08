package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

/**
 * Common interface for navbar views (tree and graph).
 */
public interface NavbarElement
{
    /**
     * Updates the view of the navbar element
     */
    void updateView();
    
    /**
     * Selects a specific path in the element
     * @param path The path to select.
     */
    void selectPath(String path);
    
    void updateSingleElement(String path);
    
    void handlePathAdded(String path);
    
    void handlePathRemoved(String path);
    
    void handlePathChanged(String path);
    
    void handlePathMoved(String path);
    
    void handlePathSorted(String path);
    
    void handleRemovedSelection(String path);
    
    void handleSettingsChanged();
}
