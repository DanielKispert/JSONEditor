package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Abstract base class for MCP (Model Context Protocol) tools.
 * Tools can be read-only or write operations on the JSON Editor model.
 */
public abstract class McpTool
{
    /**
     * @return unique tool name (e.g., "get_node", "set_node")
     */
    public abstract String getName();
    
    /**
     * @return human-readable description of what the tool does
     */
    public abstract String getDescription();
    
    /**
     * @return JSON Schema "properties" object for input parameters (ObjectNode)
     */
    public abstract ObjectNode getInputSchema();
    
    /**
     * Optional: return an output schema describing the tool result (may be null)
     */
    public ObjectNode getOutputSchema()
    {
        return null;
    }
    
    /**
     * Optional: return a JSON array of required input property names (may be null)
     */
    public ArrayNode getRequiredInputProperties()
    {
        return null;
    }
    
    /**
     * Execute the tool with given arguments.
     *
     * @param arguments tool arguments from JSON-RPC call
     * @param id JSON-RPC request id
     * @return JSON-RPC result string (formatted via createToolResult)
     * @throws JsonProcessingException if JSON serialization fails
     */
    public abstract String execute(JsonNode arguments, JsonNode id) throws JsonProcessingException;
}
