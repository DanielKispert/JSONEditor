package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.json.JsonNodeWithPath;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
    JsonNode getRootJson();
    
    JsonNodeWithPath getSelectedJsonNode();
    
    JsonSchema getSchema();
    
    boolean canAddMoreItems();
    
}
