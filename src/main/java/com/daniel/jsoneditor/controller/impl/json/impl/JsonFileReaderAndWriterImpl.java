package com.daniel.jsoneditor.controller.impl.json.impl;

import java.io.File;
import java.io.IOException;

import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import com.daniel.jsoneditor.controller.impl.json.JsonPrettyPrinter;
import com.daniel.jsoneditor.model.json.schema.CustomSchemaFactory;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFileReaderAndWriterImpl implements JsonFileReaderAndWriter
{
    private static final Logger logger = LoggerFactory.getLogger(JsonFileReaderAndWriterImpl.class);
    
    private final ObjectMapper regularMapper;
    
    private final ObjectMapper mapperIgnoringUnknownProperties;
    
    public JsonFileReaderAndWriterImpl()
    {
        this.regularMapper = new ObjectMapper();
        
        this.mapperIgnoringUnknownProperties = new ObjectMapper();
        this.mapperIgnoringUnknownProperties.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    

    
    @Override
    public JsonNode getJsonFromFile(File file)
    {
        try
        {
            return regularMapper.readTree(file);
        }
        catch (IOException e)
        {
            logger.error("Failed to read JSON from file: {}", file.getAbsolutePath(), e);
        }
        return null;
    }
    
    @Override
    public JsonNode getNodeFromString(String content) throws JsonProcessingException
    {
        if (content == null)
        {
            throw new IllegalArgumentException("Content cannot be null");
        }
        
        return regularMapper.readTree(content);
    }
    
    @Override
    public <T> T getJsonFromFile(File file, Class<T> classOfObject, boolean ignoreUnknownProperties)
    {
        try
        {
            if (ignoreUnknownProperties)
            {
                return mapperIgnoringUnknownProperties.readValue(file, classOfObject);
            }
            else
            {
                return regularMapper.readValue(file, classOfObject);
            }
        }
        catch (IOException e)
        {
            logger.error("Failed to read JSON from file {} as {}: {}", file.getAbsolutePath(), classOfObject.getSimpleName(), e.getMessage(), e);
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
        try
        {
            regularMapper.writer(new JsonPrettyPrinter()).writeValue(file, json);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }
}
