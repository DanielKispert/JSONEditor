package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.paint.Color;


public enum Toasts
{
    SAVE_SUCCESSFUL_TOAST("saved", Color.GREEN, 2),
    IMPORT_SUCCESSFUL_TOAST("Import successful", Color.GREEN, 2),
    EXPORT_SUCCESSFUL_TOAST("Export succeessful", Color.GREEN, 2),
    EXPORT_FAILED_DEPENDENCY_LOOP_TOAST("Export failed, resolve circular dependencies first", Color.RED, 4),
    SAVE_FAILED_TOAST("Save failed", Color.RED, 3),
    NO_REFERENCES_TOAST("No references found", Color.RED, 2);
    
    private final String message;
    
    private final Color color;
    
    private final int duration;
    
    Toasts(String message, Color color, int duration)
    {
        this.message = message;
        this.color = color;
        this.duration = duration;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Color getColor()
    {
        return color;
    }
    
    public int getDuration()
    {
        return duration;
    }
}
