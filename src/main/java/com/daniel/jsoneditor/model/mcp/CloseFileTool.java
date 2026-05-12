package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.CloseFileResult;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CloseFileTool extends ReadOnlyMcpTool
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public CloseFileTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "close_file";
    }

    @Override
    public String getDescription()
    {
        return "Close a previously opened file session. Cannot close GUI-owned sessions.";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        addFileIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        addFileIdRequired(arr);
        return arr;
    }

    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String fileId = getValidatedFileId(arguments);
        if (fileId == null)
        {
            return fileIdRequiredError(id);
        }

        final CloseFileResult closeResult = sessionManager.closeFile(fileId);
        if (closeResult == CloseFileResult.NOT_FOUND)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Unknown file_id: " + fileId);
        }
        if (closeResult == CloseFileResult.GUI_OWNED)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "Session is GUI-owned and cannot be closed via MCP");
        }

        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("success", true);
        result.put("file_id", fileId);

        return McpToolRegistry.createToolResult(id, result);
    }
}
