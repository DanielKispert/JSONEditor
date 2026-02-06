package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
     * @return JSON Schema for input parameters (ObjectNode with properties)
     */
    public abstract ObjectNode getInputSchema();
    
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
