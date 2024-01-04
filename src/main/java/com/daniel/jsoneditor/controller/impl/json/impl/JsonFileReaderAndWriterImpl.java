package com.daniel.jsoneditor.controller.impl.json.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import com.daniel.jsoneditor.controller.impl.json.JsonPrettyPrinter;
import com.daniel.jsoneditor.model.json.schema.CustomSchemaFactory;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

public class JsonFileReaderAndWriterImpl implements JsonFileReaderAndWriter
{
    private final ObjectMapper mapper;
    
    public JsonFileReaderAndWriterImpl()
    {
        this.mapper = new ObjectMapper();
    }
    
    @Override
    public boolean validateJsonWithSchema(JsonNode json, JsonSchema schema)
    {
        Set<ValidationMessage> messages = schema.validate(json);
        for (ValidationMessage message : messages)
        {
            System.out.println("Validation Error: " + message.getMessage() + " with element content " + json.at(convertToJSONPointer(message.getPath())));
        }
        return messages.isEmpty();
    }
    
    private String convertToJSONPointer(String path)
    {
        return path.replace("$", "").replace("[", "/").replace("]", "").replaceAll("\\.", "/");
    }
    
    @Override
    public JsonNode getJsonFromFile(File file)
    {
        try
        {
            return mapper.readTree(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public JsonNode getNodeFromString(String content)
    {
        // we validate that the content is a proper JsonNode, and transform it into that
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            
            return objectMapper.readTree(content);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public <T> T getJsonFromFile(File file, Class<T> classOfObject, boolean ignoreUnknownProperties)
    {
        try
        {
            if (ignoreUnknownProperties)
            {
                ObjectMapper newMapper = new ObjectMapper();
                newMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return newMapper.readValue(file, classOfObject);
            }
            else
            {
                return mapper.readValue(file, classOfObject);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public JsonSchema getSchemaFromFileResolvingRefs(File file)
    {
        return SchemaHelper.resolveJsonRefsInSchema(CustomSchemaFactory.makeCustomFactory().getSchema(getJsonFromFile(file)));
    }
    
    @Override
    public boolean writeJsonToFile(JsonNode json, File file)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            mapper.writer(new JsonPrettyPrinter()).writeValue(file, json);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }
}
