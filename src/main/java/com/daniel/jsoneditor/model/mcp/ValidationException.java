package com.daniel.jsoneditor.model.mcp;

/**
 * Simple exception used to indicate validation failures for MCP tool arguments.
 */
public class ValidationException extends Exception
{
    public ValidationException(final String message)
    {
        super(message);
    }
}
