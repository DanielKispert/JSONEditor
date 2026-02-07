package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


class GetReferenceableObjectsTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(GetReferenceableObjectsTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public GetReferenceableObjectsTool(final ReadableModel model)
    {
        super(model);
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
        return OBJECT_MAPPER.createObjectNode();
    }
    
    @Override
    public String execute(final JsonNode arguments, final JsonNode id) throws JsonProcessingException
    {
        final List<ReferenceableObject> objects = model.getReferenceableObjects();
        final ArrayNode result = OBJECT_MAPPER.createArrayNode();
        
        if (objects != null)
        {
            for (final ReferenceableObject obj : objects)
            {
                final ObjectNode objNode = OBJECT_MAPPER.createObjectNode();
                objNode.put("path", obj.getPath());
                objNode.put("referencing_key", obj.getReferencingKey());
                objNode.put("key_property", obj.getKey());
                result.add(objNode);
            }
        }
        
        return McpToolRegistry.createToolResult(id, result);
    }
}
