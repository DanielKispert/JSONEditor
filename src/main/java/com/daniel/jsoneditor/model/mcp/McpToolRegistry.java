package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.WritableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Registry of all available MCP tools for the JSON Editor.
 * Add/remove tools here to control which operations are exposed via MCP.
 */
public class McpToolRegistry
{
    private static final Logger logger = LoggerFactory.getLogger(McpToolRegistry.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final List<McpTool> tools;
    
    /**
     * Create registry with both read-only and write tools.
     * Write tools use WritableModel to modify the JSON document.
     * WritableModel extends ReadableModel, so read-only tools work with it too.
     */
    public McpToolRegistry(final WritableModel model)
    {
        this.tools = List.of(
                new GetCurrentFileTool(model),
                new GetNodeTool(model),
                new GetSchemaForPathTool(model),
                new GetExamplesTool(model),
                new GetReferenceableObjectsTool(model),
                new GetReferenceableInstancesTool(model)
        );
    }
    
    /**
     * @return list of all registered tools
     */
    public List<McpTool> getTools()
    {
        return tools;
    }
    
    /**
     * Find tool by name.
     *
     * @param name tool name
     * @return tool or null if not found
     */
    public McpTool getTool(final String name)
    {
        for (final McpTool tool : tools)
        {
            if (tool.getName().equals(name))
            {
                return tool;
            }
        }
        return null;
    }
    
    /**
     * Create JSON array of tool definitions for tools/list response.
     *
     * @return ArrayNode with tool definitions
     */
    public ArrayNode getToolDefinitions()
    {
        final ArrayNode toolsArray = OBJECT_MAPPER.createArrayNode();
        
        for (final McpTool tool : tools)
        {
            final ObjectNode toolDef = OBJECT_MAPPER.createObjectNode();
            toolDef.put("name", tool.getName());
            toolDef.put("description", tool.getDescription());
            
            final ObjectNode schema = OBJECT_MAPPER.createObjectNode();
            schema.put("type", "object");
            schema.set("properties", tool.getInputSchema());
            toolDef.set("inputSchema", schema);
            
            toolsArray.add(toolDef);
        }
        
        return toolsArray;
    }
    
    protected static String createToolResult(final JsonNode id, final String content) throws JsonProcessingException
    {
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        final ArrayNode contentArray = OBJECT_MAPPER.createArrayNode();
        final ObjectNode textContent = OBJECT_MAPPER.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", content);
        contentArray.add(textContent);
        result.set("content", contentArray);
        
        final ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return OBJECT_MAPPER.writeValueAsString(response);
    }
    
    protected static ObjectNode createSchemaWithProperty(final String propName, final String propType, final String description)
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        final ObjectNode prop = OBJECT_MAPPER.createObjectNode();
        prop.put("type", propType);
        prop.put("description", description);
        props.set(propName, prop);
        return props;
    }
}
