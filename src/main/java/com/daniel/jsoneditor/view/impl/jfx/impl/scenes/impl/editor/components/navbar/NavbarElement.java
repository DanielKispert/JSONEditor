package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

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
}
