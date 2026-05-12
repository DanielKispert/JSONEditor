package com.daniel.jsoneditor.model.sessions;

/**
 * Result of a {@link FileSessionManager#closeFile(String)} operation.
 */
public enum CloseFileResult
{
    /** Session was closed successfully. */
    CLOSED,
    /** No session found for the given ID. */
    NOT_FOUND,
    /** Session exists but is GUI-owned and cannot be closed via MCP. */
    GUI_OWNED
}
