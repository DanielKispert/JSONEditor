package com.daniel.jsoneditor.controller.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import java.io.File;

public interface JsonFileReaderAndWriter
{
    boolean validateJsonWithSchema(JsonNode json, JsonSchema schema);
    
    JsonNode getJsonFromFile(File file);
    
    <T> T getJsonFromFile(File file, Class<T> classOfObject);
    
    JsonSchema getSchemaFromFile(File file);
    
    boolean writeJsonToFile(JsonNode json, File file);
}
