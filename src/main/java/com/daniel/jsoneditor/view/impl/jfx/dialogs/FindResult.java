package com.daniel.jsoneditor.view.impl.jfx.dialogs;

/**
 * Wraps the result of a FindDialog, carrying both the selected path and the requested open mode.
 */
public class FindResult
{
    private final String path;
    private final boolean openInNewWindow;
    
    public FindResult(String path, boolean openInNewWindow)
    {
        this.path = path;
        this.openInNewWindow = openInNewWindow;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public boolean isOpenInNewWindow()
    {
        return openInNewWindow;
    }
}

