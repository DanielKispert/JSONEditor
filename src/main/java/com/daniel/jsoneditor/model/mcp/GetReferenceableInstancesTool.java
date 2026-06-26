package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObjectInstance;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

class GetReferenceableInstancesTool extends ReadOnlyMcpTool
{
    public GetReferenceableInstancesTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
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
        final ObjectNode props = McpToolRegistry.createSchemaWithProperty("referencing_key", "string",
                "The referencing key of the referenceable object type");
        addSessionIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addSessionIdRequired(arr);
        arr.add("referencing_key");
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

        final String referencingKey = arguments.path("referencing_key").asText("");

        final ReferenceableObject refObject = model.getReferenceableObjectByReferencingKey(referencingKey);
        if (refObject == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS, "No referenceable object found with key: " + referencingKey);
        }

        final List<ReferenceableObjectInstance> instances = model.getReferenceableObjectInstances(refObject);
        final ArrayNode result = JsonNodeFactory.instance.arrayNode();

        if (instances != null)
        {
            for (final ReferenceableObjectInstance instance : instances)
            {
                final ObjectNode instNode = JsonNodeFactory.instance.objectNode();
                instNode.put("path", instance.getPath());
                instNode.put("key", instance.getKey());
                instNode.put("display_name", instance.getFancyName());
                result.add(instNode);
            }
        }

        return McpToolRegistry.createToolResult(id, result);
    }
}
