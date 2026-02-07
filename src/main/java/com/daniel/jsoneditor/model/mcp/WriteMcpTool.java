package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.WritableModel;


/**
 * Base class for write MCP tools that can modify the model state.
 * These tools can change the JSON document via WritableModel operations.
 */
public abstract class WriteMcpTool extends McpTool
{
    protected final WritableModel model;
    
    protected WriteMcpTool(final WritableModel model)
    {
        if (model == null)
        {
            throw new IllegalArgumentException("model cannot be null");
        }
        this.model = model;
    }
}
