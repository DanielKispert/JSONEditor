package com.daniel.jsoneditor.controller.impl.json.impl;

import com.daniel.jsoneditor.controller.impl.json.JsonPrettyPrinter;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
            System.out.println("Validation Error: " + message.getMessage());
        }
        return messages.size() == 0;
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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return SchemaHelper.resolveJsonRefsInSchema(factory.getSchema(getJsonFromFile(file)));
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
