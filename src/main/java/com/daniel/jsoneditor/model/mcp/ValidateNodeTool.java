package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class ValidateNodeTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(ValidateNodeTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ValidateNodeTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "validate_node";
    }

    @Override
    public String getDescription()
    {
        return "Validate whether a JSON value would be schema-valid at a specific path";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = McpToolRegistry.createSchemaWithProperty("path", "string",
                "JSON pointer where the value would be validated (e.g., /items/0)");
        addFileIdProperty(props);
        final ObjectNode contentProp = OBJECT_MAPPER.createObjectNode();
        contentProp.put("description", "The JSON value to validate (object, array, string, number, boolean, or null)");
        props.set("content", contentProp);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        addFileIdRequired(arr);
        arr.add("path");
        arr.add("content");
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

        final String path = arguments.path("path").asText("");

        final JsonSchema schema = model.getSubschemaForPath(path);
        if (schema == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "No schema found for path: " + path);
        }

        final JsonNode content = arguments.get("content");
        if (content == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "content argument is required");
        }

        final List<String> errors;
        try
        {
            errors = SchemaHelper.validateJsonWithSchema(content, schema);
        }
        catch (Exception e)
        {
            logger.warn("Schema validation failed unexpectedly for path {}: {}", path, e.getMessage(), e);
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "Schema validation failed: " + e.getMessage());
        }

        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        result.put("valid", errors.isEmpty());
        final ArrayNode errorsArray = OBJECT_MAPPER.createArrayNode();
        for (final String error : errors)
        {
            errorsArray.add(error);
        }
        result.set("errors", errorsArray);

        return McpToolRegistry.createToolResult(id, result);
    }
}
