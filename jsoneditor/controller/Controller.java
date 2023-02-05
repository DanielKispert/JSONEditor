package jsoneditor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jsoneditor.model.json.JsonNodeWithPath;

import java.io.File;

public interface Controller
{

    void launchFinished();
    
    void jsonAndSchemaSelected(File json, File schema);
    
    void chooseNodeFromNavbar(JsonNodeWithPath node);
    
    void removeNodeFromArray(JsonNode node);
    
    void removeSelectedNode();
    

}
