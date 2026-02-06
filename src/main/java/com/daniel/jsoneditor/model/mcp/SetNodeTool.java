package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.WritableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Example write tool that sets a value at a specific path.
 * Uncomment in McpToolRegistry to enable.
 */
class SetNodeTool extends WriteMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(SetNodeTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public SetNodeTool(final WritableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "set_node";
    }
    
    @Override
    public String getDescription()
    {
        return "Set a value at a specific path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        
        final ObjectNode pathProp = OBJECT_MAPPER.createObjectNode();
        pathProp.put("type", "string");
        pathProp.put("description", "JSON path (e.g., /root/child)");
        props.set("path", pathProp);
        
        final ObjectNode propertyProp = OBJECT_MAPPER.createObjectNode();
        propertyProp.put("type", "string");
        propertyProp.put("description", "Property name to set");
        props.set("property", propertyProp);
        
        final ObjectNode valueProp = OBJECT_MAPPER.createObjectNode();
        valueProp.put("description", "Value to set (string, number, boolean, null)");
        props.set("value", valueProp);
        
        return props;
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        final String property = arguments.path("property").asText("");
        
        if (path.isEmpty() || property.isEmpty())
        {
            return McpToolRegistry.createToolResult(id, "Error: path and property parameters are required");
        }
        
        final JsonNode valueNode = arguments.path("value");
        if (valueNode.isMissingNode())
        {
            return McpToolRegistry.createToolResult(id, "Error: value parameter is required");
        }
        
        try
        {
            final Object value;
            if (valueNode.isTextual())
            {
                value = valueNode.asText();
            }
            else if (valueNode.isNumber())
            {
                value = valueNode.numberValue();
            }
            else if (valueNode.isBoolean())
            {
                value = valueNode.asBoolean();
            }
            else if (valueNode.isNull())
            {
                value = null;
            }
            else
            {
                return McpToolRegistry.createToolResult(id, "Error: value must be string, number, boolean, or null");
            }
            
            model.setValueAtPath(path, property, value);
            
            final ObjectNode result = OBJECT_MAPPER.createObjectNode();
            result.put("success", true);
            result.put("path", path);
            result.put("property", property);
            
            return McpToolRegistry.createToolResult(id, OBJECT_MAPPER.writeValueAsString(result));
        }
        catch (Exception e)
        {
            logger.error("Error executing set_node for path: {}, property: {}", path, property, e);
            return McpToolRegistry.createToolResult(id,
                    String.format("Error: Failed to set value at path: %s, property: %s", path, property));
        }
    }
}
