package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.statemachine.impl.State;

import java.io.File;

public interface WritableModel
{
    void setCurrentJSONFile(File json);
    
    void setCurrentSchemaFile(File schema);
    
    void setJson(JsonNode json);
    
    void setSchema(JsonSchema schema);
    
    void setState(State state);
}
