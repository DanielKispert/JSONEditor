package com.daniel.jsoneditor.controller.settings;

import com.daniel.jsoneditor.controller.settings.impl.EditorDimensions;


public interface SettingsController
{
    void setFileProperties(boolean rememberPaths, String jsonPath, String schemaPath, String settingsPath);
    void setHideEmptyColumns(boolean automaticallyHideEmptyColumns);
    
    boolean hideEmptyColumns();
    
    void setRenameReferencesWhenRenamingObject(boolean renameReferences);
    
    void setClusterShape(String symbol);
    
    void setEditorDimensions(int width, int height, boolean startMaximized);
    
    EditorDimensions getEditorDimensions();
    
    String getClusterShape();
    
    boolean renameReferencesWhenRenamingObject();
    
    String getLastJsonPath();
    
    String getLastSchemaPath();
    
    String getLastSettingsPath();
    
    boolean rememberPaths();
    
    void setDebugMode(boolean debugMode);
    
    boolean isDebugMode();
    
    void setLogGraphRequests(boolean logGraphRequests);
    
    boolean isLogGraphRequests();
    
    void setMcpServerEnabled(boolean enabled);
    
    boolean isMcpServerEnabled();
    
    void setMcpServerPort(int port);
    
    int getMcpServerPort();
    
    void setNavbarCollapsed(boolean collapsed);
    
    boolean isNavbarCollapsed();
    
    /**
     * Sets the maximum number of editor windows. Use "auto" to base it on window width, or a number string between 3 and 10.
     */
    void setMaxEditorWindows(String maxWindows);
    
    /**
     * Returns the configured max editor windows value. "auto" means based on window width, otherwise a number string.
     */
    String getMaxEditorWindows();
    
    /**
     * Sets the default behavior for whether dialogs should open results in a new window.
     */
    void setOpenInNewWindow(boolean openInNewWindow);
    
    /**
     * Returns whether dialogs should default to opening results in a new window.
     */
    boolean isOpenInNewWindow();
}
