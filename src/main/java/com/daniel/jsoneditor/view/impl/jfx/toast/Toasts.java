package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.paint.Color;


public enum Toasts implements ToastLike
{
    SAVE_SUCCESSFUL_TOAST("saved", Color.GREEN),
    IMPORT_SUCCESSFUL_TOAST("Import successful", Color.GREEN),
    IMPORT_VALIDATION_FAILED_TOAST("Import failed: Schema validation error", Color.RED),
    IMPORT_PARSING_FAILED_TOAST("Import failed: Invalid JSON format", Color.RED),
    EXPORT_SUCCESSFUL_TOAST("Export succeessful", Color.GREEN),
    REFRESH_SUCCESSFUL_TOAST("Reloaded", Color.GREEN),
    COPIED_TO_CLIPBOARD_TOAST("Copied to clipboard", Color.GREEN),
    ERROR_TOAST("Error", Color.RED),
    EXPORT_FAILED_DEPENDENCY_LOOP_TOAST("Export failed, resolve circular dependencies first", Color.RED),
    PASTED_FROM_CLIPBOARD_TOAST("Pasted", Color.GREEN),
    NO_REFERENCES_TOAST("No references found", Color.RED),
    NO_DIFFERENCES_TOAST("No differences found", Color.GREEN);
    
    private final String message;
    
    private final Color color;
    
    Toasts(String message, Color color)
    {
        this.message = message;
        this.color = color;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Color getColor()
    {
        return color;
    }
    
}
