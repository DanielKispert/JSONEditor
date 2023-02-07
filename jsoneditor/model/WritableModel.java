package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.statemachine.impl.Event;

import java.io.File;

public interface WritableModel
{
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void sendEvent(Event state);
    
    void selectJsonNode(JsonNodeWithPath node);
    
    void removeNodeFromSelectedArray(JsonNode node);
    
    void addNodeToSelectedArray();
    
    void removeSelectedNode();
}
