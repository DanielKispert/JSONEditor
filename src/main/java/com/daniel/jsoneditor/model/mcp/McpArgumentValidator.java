package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lightweight validator for MCP tool arguments based on the tool's input schema.
 * Uses networknt JsonSchema validation library already present in the project.
 */
public final class McpArgumentValidator
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    
    private McpArgumentValidator() { /* utility */ }

    /**
     * Validate arguments against input schema using JSON Schema validation.
     *
     * @param inputSchema ObjectNode representing the inputSchema with properties
     * @param arguments JsonNode with actual arguments
     * @throws ValidationException if validation fails
     */
    public static void validate(final ObjectNode inputSchema, final JsonNode arguments) throws ValidationException
    {
        if (inputSchema == null || inputSchema.isEmpty())
        {
            return;
        }

        if (arguments == null || arguments.isMissingNode() || arguments.isNull())
        {
            return;
        }

        final ObjectNode schemaNode = buildValidationSchema(inputSchema);
        final JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
        final Set<ValidationMessage> errors = schema.validate(arguments);

        if (!errors.isEmpty())
        {
            final String errorMessage = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ValidationException("Invalid parameters: " + errorMessage);
        }
    }

    private static ObjectNode buildValidationSchema(final ObjectNode inputSchema)
    {
        final ObjectNode schemaNode = MAPPER.createObjectNode();
        schemaNode.put("type", "object");
        schemaNode.set("properties", inputSchema);
        schemaNode.put("additionalProperties", false);
        return schemaNode;
    }
}
