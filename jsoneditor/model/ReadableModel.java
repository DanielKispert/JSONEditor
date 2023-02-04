package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
    JsonNode getRootJson();
    
    JsonNode getSelectedJsonNode();
    
    String getNameOfSelectedJsonNode();
    
}
