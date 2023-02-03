package jsoneditor.controller.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import java.io.File;

public interface JsonReader
{
    boolean validateJsonWithSchema(JsonNode json, JsonSchema schema);
    
    JsonNode getJsonFromFile(File file);
    
    JsonSchema getSchemaFromFile(File file);
}
