package com.daniel.jsoneditor.controller.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import java.io.File;

public interface JsonFileReaderAndWriter
{
    JsonNode getJsonFromFile(File file);
    
    JsonNode getNodeFromString(String content);
    
    <T> T getJsonFromFile(File file, Class<T> classOfObject, boolean ignoreUnknownProperties);
    
    JsonSchema getSchemaFromFileResolvingRefs(File file);
    
    boolean writeJsonToFile(JsonNode json, File file);
}
