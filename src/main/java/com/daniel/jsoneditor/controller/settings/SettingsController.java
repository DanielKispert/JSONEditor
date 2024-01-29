package com.daniel.jsoneditor.controller.settings;

public interface SettingsController
{
    void setFileProperties(boolean rememberPaths, String jsonPath, String schemaPath, String settingsPath);
    void setHideEmptyColumns(boolean automaticallyHideEmptyColumns);
    
    boolean hideEmptyColumns();
    
    void setRenameReferencesWhenRenamingObject(boolean renameReferences);
    
    boolean renameReferencesWhenRenamingObject();
    
    String getLastJsonPath();
    
    String getLastSchemaPath();
    
    String getLastSettingsPath();
    
    boolean rememberPaths();
}
