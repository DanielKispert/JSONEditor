package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

class FindReferencesToTool extends ReadOnlyMcpTool
{

    public FindReferencesToTool(final FileSessionManager sessionManager)
    {
        super(sessionManager);
    }

    @Override
    public String getName()
    {
        return "find_references_to";
    }

    @Override
    public String getDescription()
    {
        return "Find all references pointing to a referenceable object instance at a given path";
    }

    @Override
    public ObjectNode getInputSchema()
    {
        final ObjectNode props = McpToolRegistry.createSchemaWithProperty("path", "string",
                "JSON path to a referenceable object instance to find references to (e.g., /items/0)");
        addSessionIdProperty(props);
        return props;
    }

    @Override
    public ArrayNode getRequiredInputProperties()
    {
        final ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        addSessionIdRequired(arr);
        arr.add("path");
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

        final List<ReferenceToObjectInstance> references = model.getReferencesToObjectForPath(path);

        final ArrayNode result = JsonNodeFactory.instance.arrayNode();

        if (references != null)
        {
            for (final ReferenceToObjectInstance ref : references)
            {
                final ObjectNode refNode = JsonNodeFactory.instance.objectNode();
                refNode.put("path", ref.getPath());
                refNode.put("key", ref.getKey());
                refNode.put("display_name", ref.getFancyName());
                refNode.put("referencing_key", ref.getReference().getObjectReferencingKey());

                final String remarks = ref.getRemarks();
                if (remarks != null)
                {
                    refNode.put("remarks", remarks);
                }
                else
                {
                    refNode.putNull("remarks");
                }

                result.add(refNode);
            }
        }

        return McpToolRegistry.createToolResult(id, result);
    }
}
