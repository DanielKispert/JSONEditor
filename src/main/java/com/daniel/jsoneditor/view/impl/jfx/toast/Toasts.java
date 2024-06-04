package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.paint.Color;


public enum Toasts
{
    SAVE_SUCCESSFUL_TOAST("Save successful", Color.GREEN, 3),
    SAVE_FAILED_TOAST("Save failed", Color.RED, 3);
    
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
