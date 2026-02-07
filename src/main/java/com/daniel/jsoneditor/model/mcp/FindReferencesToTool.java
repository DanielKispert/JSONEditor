package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


class FindReferencesToTool extends ReadOnlyMcpTool
{
    private static final Logger logger = LoggerFactory.getLogger(FindReferencesToTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    public FindReferencesToTool(final ReadableModel model)
    {
        super(model);
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
        return McpToolRegistry.createSchemaWithProperty("path", "string",
                "JSON path to a referenceable object instance to find references to");
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
        
        final List<ReferenceToObjectInstance> references = model.getReferencesToObjectForPath(path);
        
        final ArrayNode result = OBJECT_MAPPER.createArrayNode();
        
        if (references != null)
        {
            for (final ReferenceToObjectInstance ref : references)
            {
                final ObjectNode refNode = OBJECT_MAPPER.createObjectNode();
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
