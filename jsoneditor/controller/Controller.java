package jsoneditor.controller;

import jsoneditor.model.json.JsonNodeWithPath;

import java.io.File;

public interface Controller
{

    void launchFinished();
    
    void jsonAndSchemaSelected(File json, File schema, File settings);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    void removeNode(String path);
    
    void addNewNodeToArray(String path);
    
    void saveToFile();
    

}
