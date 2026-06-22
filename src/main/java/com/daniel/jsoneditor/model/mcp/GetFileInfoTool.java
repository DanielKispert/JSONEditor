package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GetFileInfoTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetFileInfoTool.class);

    public GetFileInfoTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "get_file_info";
    }

    @Override
    public String getDescription()
    {
        return "Get information about an open JSON file and schema";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = JsonNodeFactory.instance.objectNode();
        addFileIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addFileIdRequired(arr);
        return arr;
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final var resolved = resolveFileSession(arguments, id);
        if (resolved.error() != null)
        {
            return resolved.error();
        }
        final ReadableModel model = resolved.model();

        final ObjectNode content = JsonNodeFactory.instance.objectNode();

        if (model.getCurrentJSONFile() != null)
        {
            content.put("file_path", model.getCurrentJSONFile().getAbsolutePath());
            content.put("file_name", model.getCurrentJSONFile().getName());
        }
        else
        {
            content.putNull("file_path");
            content.putNull("file_name");
        }

        if (model.getCurrentSchemaFile() != null)
        {
            content.put("schema_path", model.getCurrentSchemaFile().getAbsolutePath());
        }
        else
        {
            content.putNull("schema_path");
        }

        content.put("has_content", model.getRootJson() != null);

        return McpToolRegistry.createToolResult(id, content);
    }
}
