package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.settings.Settings;
import jsoneditor.model.statemachine.impl.Event;

import java.io.File;
import java.util.Properties;

public interface WritableModel
{
    
    void jsonAndSchemaSuccessfullyValidated(File jsonFile, File schemaFile, JsonNode json, JsonSchema schema);
    
    void setSettings(Settings settings);
    
    void sendEvent(Event state);
    
    void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index);
    
    void addNodeToArray(String selectedPath);
    
    void removeNode(String path);
}
