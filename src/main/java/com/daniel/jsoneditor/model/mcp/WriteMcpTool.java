package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.FileSessionManager;


/**
 * Base class for write MCP tools that can modify the model state.
 */
public abstract class WriteMcpTool extends ReadOnlyMcpTool
{
    protected WriteMcpTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }
}
