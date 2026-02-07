package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GetFileInfoTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetFileInfoTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetFileInfoTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_file_info";
    }
    
    @Override
    public String getDescription()
    {
        return "Get information about the currently open JSON file and schema";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return OBJECT_MAPPER.createObjectNode();
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final ObjectNode content = OBJECT_MAPPER.createObjectNode();
        
        if (model.getCurrentJSONFile() != null)
        {
            content.put("file_path", model.getCurrentJSONFile().getAbsolutePath());
            content.put("file_name", model.getCurrentJSONFile().getName());
        }
        else
        {
            content.putNull("file_path");
            content.putNull("file_name");
        }
        
        if (model.getCurrentSchemaFile() != null)
        {
            content.put("schema_path", model.getCurrentSchemaFile().getAbsolutePath());
        }
        else
        {
            content.putNull("schema_path");
        }
        
        content.put("has_content", model.getRootJson() != null);
        
        return McpToolRegistry.createToolResult(id, content);
    }
}
