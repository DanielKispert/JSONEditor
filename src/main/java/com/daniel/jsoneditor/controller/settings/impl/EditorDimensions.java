package com.daniel.jsoneditor.controller.settings.impl;

public class EditorDimensions
{
    private final int width;
    
    private final int height;
    
    private final boolean maximized;
    
    public EditorDimensions(int width, int height, boolean maximized)
    {
        this.width = width;
        this.height = height;
        this.maximized = maximized;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public boolean isMaximized()
    {
        return maximized;
    }
}
