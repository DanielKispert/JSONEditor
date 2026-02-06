package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


class GetReferenceableInstancesTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetReferenceableInstancesTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetReferenceableInstancesTool(final ReadableModel model)
    {
        super(model);
    }
    
    @Override
    public String getName()
    {
        return "get_referenceable_instances";
    }
    
    @Override
    public String getDescription()
    {
        return "Get all instances of a referenceable object type";
    }
    
    @Override
    public ObjectNode getInputSchema()
    {
        return McpToolRegistry.createSchemaWithProperty("referencing_key", "string",
                "The referencing key of the referenceable object type");
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        arr.add("referencing_key");
        return arr;
    }
    
    @Override
    public ObjectNode getOutputSchema()
    {
        final ObjectNode item = OBJECT_MAPPER.createObjectNode();
        item.set("path", McpToolRegistry.createSchemaWithProperty("path", "string", ""));
        item.set("key", McpToolRegistry.createSchemaWithProperty("key", "string", ""));
        item.set("display_name", McpToolRegistry.createSchemaWithProperty("display_name", "string", ""));
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        props.set("items", item);
        return props;
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final String referencingKey = arguments.path("referencing_key").asText("");
        
        final ReferenceableObject refObject = model.getReferenceableObjectByReferencingKey(referencingKey);
        if (refObject == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, -32602, "No referenceable object found with key: " + referencingKey);
        }
        
        final List<ReferenceableObjectInstance> instances = model.getReferenceableObjectInstances(refObject);
        final ArrayNode result = OBJECT_MAPPER.createArrayNode();
        
        if (instances != null)
        {
            for (final ReferenceableObjectInstance instance : instances)
            {
                final ObjectNode instNode = OBJECT_MAPPER.createObjectNode();
                instNode.put("path", instance.getPath());
                instNode.put("key", instance.getKey());
                instNode.put("display_name", instance.getFancyName());
                result.add(instNode);
            }
        }
        
        return McpToolRegistry.createToolResult(id, result);
    }
}
