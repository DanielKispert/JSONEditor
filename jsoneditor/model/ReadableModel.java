package jsoneditor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import jsoneditor.model.json.JsonNodeWithPath;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
    JsonNode getRootJson();
    
    JsonNodeWithPath getSelectedJsonNode();
    
    JsonNode getSchemaNodeOfSelectedNode();
    
    JsonSchema getRootSchema();
    
    /**
     * @return true if the currently selected node is an array and can have more items, false if not
     */
    boolean canAddMoreItems();
    
    JsonNodeWithPath getNodeForPath(String path);
    
}
