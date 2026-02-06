package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


class GetExamplesTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetExamplesTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetExamplesTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_examples";
    }
    
    @Override
    public String getDescription()
    {
        return "Get example values for a JSON path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return McpToolRegistry.createSchemaWithProperty("path", "string", "JSON path to get examples for");
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
            final List<String> examples = model.getStringExamplesForPath(path);
            final List<String> allowedValues = model.getAllowedStringValuesForPath(path);
            
            final ObjectNode result = OBJECT_MAPPER.createObjectNode();
            
            final ArrayNode examplesArray = OBJECT_MAPPER.createArrayNode();
            if (examples != null)
            {
                examples.forEach(examplesArray::add);
            }
            result.set("examples", examplesArray);
            
            final ArrayNode allowedArray = OBJECT_MAPPER.createArrayNode();
            if (allowedValues != null)
            {
                allowedValues.forEach(allowedArray::add);
            }
            result.set("allowed_values", allowedArray);
            
            return McpToolRegistry.createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
        }
        catch (Exception e)
        {
            logger.error("Error executing get_examples for path: {}", path, e);
            return McpToolRegistry.createToolResult(id, String.format("Error: Failed to get examples for path: %s", path));
        }
    }
}
