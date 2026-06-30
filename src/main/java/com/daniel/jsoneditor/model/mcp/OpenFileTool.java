package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Deprecated alias for {@link OpenSessionTool}. Use open_session instead.
 */
class OpenFileTool extends McpTool
{
    private final OpenSessionTool delegate;

    public OpenFileTool(final FileSessionManager sessionManager)
    {
        this.delegate = new OpenSessionTool(sessionManager);
    }

    @Override
    public String getName()
    {
        return "open_file";
    }

    @Override
    public String getDescription()
    {
        return "[DEPRECATED — use open_session instead.] " + delegate.getDescription();
    }

    @Override
    public ObjectNode getInputSchema()
    {
        return delegate.getInputSchema();
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        return delegate.getRequiredInputProperties();
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        return delegate.execute(arguments, id);
    }
}
