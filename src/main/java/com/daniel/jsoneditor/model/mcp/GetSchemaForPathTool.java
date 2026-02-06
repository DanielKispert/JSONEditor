package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GetSchemaForPathTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetSchemaForPathTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetSchemaForPathTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_schema_for_path";
    }
    
    @Override
    public String getDescription()
    {
        return "Get the JSON schema definition for a specific path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return McpToolRegistry.createSchemaWithProperty("path", "string", "JSON path to get schema for");
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        if (path.isEmpty())
        {
            return McpToolRegistry.createToolResult(id, "Error: path parameter is required");
        }
        
        try
        {
            final JsonSchema schema = model.getSubschemaForPath(path);
            if (schema == null)
            {
                return McpToolRegistry.createToolResult(id, String.format("Error: No schema found for path: %s", path));
            }
            
            return McpToolRegistry.createToolResult(id, schema.getSchemaNode().toString());
        }
        catch (Exception e)
        {
            logger.error("Error executing get_schema_for_path for path: {}", path, e);
            return McpToolRegistry.createToolResult(id, String.format("Error: Failed to get schema for path: %s", path));
        }
    }
}
