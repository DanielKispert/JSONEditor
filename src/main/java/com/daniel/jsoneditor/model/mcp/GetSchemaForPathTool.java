package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class GetSchemaForPathTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetSchemaForPathTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetSchemaForPathTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_schema_for_path";
    }
    
    @Override
    public String getDescription()
    {
        return "Get the JSON schema definition for a specific path";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return McpToolRegistry.createSchemaWithProperty("path", "string", "JSON path to get schema for");
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        arr.add("path");
        return arr;
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String path = arguments.path("path").asText("");
        
        final JsonSchema schema = model.getSubschemaForPath(path);
        if (schema == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, -32602, "No schema found for path: " + path);
        }
        
        final ObjectNode out = OBJECT_MAPPER.createObjectNode();
        out.set("schema", OBJECT_MAPPER.readTree(schema.getSchemaNode().toString()));
        return McpToolRegistry.createToolResult(id, out);
    }
}
