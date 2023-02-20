package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.settings.Settings;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
    JsonNode getRootJson();
    
    JsonNodeWithPath getSelectedJsonNode();
    
    JsonNode getSchemaNodeOfSelectedNode();
    
    JsonSchema getRootSchema();
    
    Settings getSettings();
    
    /**
     * @return true if the currently selected node is an array and can have more items, false if not
     */
    boolean canAddMoreItems();
    
    boolean editingAnArray();
    
    boolean editingAnObject();
    
    JsonNodeWithPath getNodeForPath(String path);
    
}
