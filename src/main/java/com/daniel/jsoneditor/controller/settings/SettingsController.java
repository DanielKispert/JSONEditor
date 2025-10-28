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
}
