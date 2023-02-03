package jsoneditor.controller.impl.json.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jsoneditor.controller.impl.json.JsonReader;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class JsonReaderImpl implements JsonReader
{
    private final ObjectMapper mapper;
    
    public JsonReaderImpl()
    {
        this.mapper = new ObjectMapper();
    }
    
    @Override
    public boolean validateJsonWithSchema(JsonNode json, JsonSchema schema)
    {
        Set<ValidationMessage> messages = schema.validate(json);
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
    public JsonSchema getSchemaFromFile(File file)
    {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return factory.getSchema(getJsonFromFile(file));
    }
    
}
