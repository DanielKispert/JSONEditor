package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for MCP tool arguments using JSON Schema validation.
 * Accepts a complete JSON Schema object (with properties, required, etc.)
 * and validates arguments against it using the networknt library.
 */
public final class McpArgumentValidator
{
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    
    private McpArgumentValidator() { /* utility */ }

    /**
     * Validate arguments against a complete JSON Schema.
     *
     * @param schemaNode Complete JSON Schema (type=object, properties, required, etc.)
     * @param arguments JsonNode with actual arguments
     * @throws ValidationException if validation fails with all error messages
     */
    public static void validate(final JsonNode schemaNode, final JsonNode arguments) throws ValidationException
    {
        if (schemaNode == null || schemaNode.isEmpty())
        {
            return;
        }

        if (arguments == null || arguments.isMissingNode())
        {
            if (schemaNode.has("required") && schemaNode.get("required").size() > 0)
            {
                throw new ValidationException("Missing required parameters");
            }
            return;
        }

        final JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
        final Set<ValidationMessage> errors = schema.validate(arguments);

        if (!errors.isEmpty())
        {
            final String errorMessage = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ValidationException(errorMessage);
        }
    }
}
