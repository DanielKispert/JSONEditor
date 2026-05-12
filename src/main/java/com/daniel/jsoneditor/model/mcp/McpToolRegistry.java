package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.controller.AppService;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of all available MCP tools for the JSON Editor.
 * Add/remove tools here to control which operations are exposed via MCP.
 */
public class McpToolRegistry
{
    private static final Logger logger = LoggerFactory.getLogger(McpToolRegistry.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<McpTool> tools;

    /**
     * Create registry with all available tools. Pass null for headless mode. When
     * appService is provided, the show_gui tool is registered for opening GUI
     * windows on demand.
     *
     * @param sessionManager manages all open file sessions
     * @param appService the app service for GUI integration, or null for headless mode
     */
    public McpToolRegistry(final FileSessionManager sessionManager, final AppService appService)
    {
        final List<McpTool> toolList = new ArrayList<>();
        toolList.add(new ListFilesTool(sessionManager));
        toolList.add(new OpenFileTool(sessionManager));
        toolList.add(new CloseFileTool(sessionManager));
        toolList.add(new GetFileInfoTool(sessionManager));
        toolList.add(new GetNodeTool(sessionManager));
        toolList.add(new GetSchemaForPathTool(sessionManager));
        toolList.add(new GetExamplesTool(sessionManager));
        toolList.add(new GetReferenceableObjectsTool(sessionManager));
        toolList.add(new GetReferenceableInstancesTool(sessionManager));
        toolList.add(new FindReferencesToTool(sessionManager));
        if (appService != null)
        {
            toolList.add(new ShowGuiTool(appService));
        }
        this.tools = List.copyOf(toolList);
    }

    /**
     * @return list of all registered tools
     */
    public List<McpTool> getTools()
    {
        return tools;
    }

    /**
     * Find tool by name.
     *
     * @param name tool name
     * @return tool or null if not found
     */
    public McpTool getTool(final String name)
    {
        for (final McpTool tool : tools)
        {
            if (tool.getName().equals(name))
            {
                return tool;
            }
        }
        return null;
    }

    /**
     * Create JSON array of tool definitions for tools/list response.
     *
     * @return ArrayNode with tool definitions
     */
    public ArrayNode getToolDefinitions()
    {
        final ArrayNode toolsArray = OBJECT_MAPPER.createArrayNode();

        for (final McpTool tool : tools)
        {
            final ObjectNode toolDef = OBJECT_MAPPER.createObjectNode();
            toolDef.put("name", tool.getName());
            toolDef.put("description", tool.getDescription());

            toolDef.set("inputSchema", buildInputSchema(tool));

            toolsArray.add(toolDef);
        }

        return toolsArray;
    }

    /**
     * Build complete input schema for a tool including type, properties, required, and additionalProperties.
     * This is the canonical schema used both for tools/list and for validation.
     */
    public static ObjectNode buildInputSchema(final McpTool tool)
    {
        final ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", tool.getInputSchema());
        schema.put("additionalProperties", false);

        final ArrayNode required = tool.getRequiredInputProperties();
        if (required != null && !required.isEmpty())
        {
            schema.set("required", required);
        }

        return schema;
    }

    /**
     * Create a tool result where content contains a single text element with JSON payload.
     * The payload is serialized as a JSON string in the "text" field per MCP specification.
     */
    protected static String createToolResult(final JsonNode id, final JsonNode payload) throws JsonProcessingException
    {
        final ObjectNode result = OBJECT_MAPPER.createObjectNode();
        final ArrayNode contentArray = OBJECT_MAPPER.createArrayNode();
        final ObjectNode textContent = OBJECT_MAPPER.createObjectNode();
        textContent.put("type", "text");
        final String jsonText = OBJECT_MAPPER.writeValueAsString(payload == null ? OBJECT_MAPPER.nullNode() : payload);
        textContent.put("text", jsonText);
        contentArray.add(textContent);
        result.set("content", contentArray);

        final ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return OBJECT_MAPPER.writeValueAsString(response);
    }

    protected static ObjectNode createSchemaWithProperty(final String propName, final String propType, final String description)
    {
        final ObjectNode props = OBJECT_MAPPER.createObjectNode();
        final ObjectNode prop = OBJECT_MAPPER.createObjectNode();
        prop.put("type", propType);
        prop.put("description", description);
        props.set(propName, prop);
        return props;
    }
}
