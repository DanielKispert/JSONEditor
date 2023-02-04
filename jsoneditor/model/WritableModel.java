package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.statemachine.impl.State;

import java.io.File;

public interface WritableModel
{
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void setState(State state);
    
    void selectJsonNode(String name, JsonNode jsonNode);
}
