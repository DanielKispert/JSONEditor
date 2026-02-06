package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GetNodeTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetNodeTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetNodeTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_node";
    }
    
    @Override
    public String getDescription()
    {
        return "Get a JSON node at a specific path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return McpToolRegistry.createSchemaWithProperty("path", "string", "JSON path (e.g., /root/child)");
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
            final JsonNodeWithPath node = model.getNodeForPath(path);
            if (node == null)
            {
                return McpToolRegistry.createToolResult(id, String.format("Error: No node found at path: %s", path));
            }
            
            final ObjectNode result = OBJECT_MAPPER.createObjectNode();
            result.put("path", node.getPath());
            result.put("display_name", node.getDisplayName());
            result.set("value", node.getNode());
            result.put("is_array", node.isArray());
            result.put("is_object", node.getNode().isObject());
            
            return McpToolRegistry.createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
        }
        catch (Exception e)
        {
            logger.error("Error executing get_node for path: {}", path, e);
            return McpToolRegistry.createToolResult(id, String.format("Error: Failed to get node at path: %s", path));
        }
    }
}
