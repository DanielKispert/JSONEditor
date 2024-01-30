package com.daniel.jsoneditor.view.impl.jfx;

public interface UIHandler
{
    
    void showSelectJsonAndSchema();
    
    void showMainEditor();
    
    void handleAddedArrayItem(String pathOfArrayItem);
    
    void updateEditorSceneWithSelectedJson();
    
    void updateEditorSceneWithRemovedJson();
    
    void updateEditorSceneWithUpdatedStructure();
    
    void updateEditorSceneWithMovedJson();
    
}
