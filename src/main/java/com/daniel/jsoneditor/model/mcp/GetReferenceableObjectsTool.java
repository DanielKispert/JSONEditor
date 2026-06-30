package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

class GetReferenceableObjectsTool extends ReadOnlyMcpTool
{
    public GetReferenceableObjectsTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "get_referenceable_objects";
    }

    @Override
    public String getDescription()
    {
        return "List all referenceable object types defined in the schema";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = JsonNodeFactory.instance.objectNode();
        addSessionIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addSessionIdRequired(arr);
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

        final List<ReferenceableObject> objects = model.getReferenceableObjects();
        final ArrayNode result = JsonNodeFactory.instance.arrayNode();

        if (objects != null)
        {
            for (final ReferenceableObject obj : objects)
            {
                final ObjectNode objNode = JsonNodeFactory.instance.objectNode();
                objNode.put("path", obj.getPath());
                objNode.put("referencing_key", obj.getReferencingKey());
                objNode.put("key_property", obj.getKey());
                result.add(objNode);
            }
        }

        return McpToolRegistry.createToolResult(id, result);
    }
}
