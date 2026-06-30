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

class FindReferenceableInstanceTool extends ReadOnlyMcpTool
{
    public FindReferenceableInstanceTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "find_referenceable_instance";
    }

    @Override
    public String getDescription()
    {
        return "Find a single referenceable object instance by its key. Returns {path, key, display_name} or null if not found.";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = McpToolRegistry.createSchemaWithProperty("referencing_key", "string",
                "The referencing key of the referenceable object type");
        addSessionIdProperty(props);
        final ObjectNode instanceKeyProp = JsonNodeFactory.instance.objectNode();
        instanceKeyProp.put("type", "string");
        instanceKeyProp.put("description", "The key value of the specific instance to find");
        props.set("instance_key", instanceKeyProp);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addSessionIdRequired(arr);
        arr.add("referencing_key");
        arr.add("instance_key");
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
        final String instanceKey = arguments.path("instance_key").asText("");

        final ReferenceableObject refObject = model.getReferenceableObjectByReferencingKey(referencingKey);
        if (refObject == null)
        {
            return JsonEditorMcpServer.createErrorResponseStatic(id, JSONRPC_INVALID_PARAMS,
                    "No referenceable object found with key: " + referencingKey);
        }

        final ReferenceableObjectInstance instance = model.getReferenceableObjectInstanceWithKey(refObject, instanceKey);
        if (instance == null)
        {
            return McpToolRegistry.createToolResult(id, JsonNodeFactory.instance.nullNode());
        }

        final ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("path", instance.getPath());
        result.put("key", instance.getKey());
        result.put("display_name", instance.getFancyName());
        return McpToolRegistry.createToolResult(id, result);
    }
}
