package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;


/**
 * Base class for read-only MCP tools that only query the model state.
 * These tools cannot modify the JSON document.
 */
public abstract class ReadOnlyMcpTool extends McpTool
{
    protected final ReadableModel model;
    
    protected ReadOnlyMcpTool(final ReadableModel model)
    {
        if (model == null)
        {
            throw new IllegalArgumentException("model cannot be null");
        }
        this.model = model;
    }
}
