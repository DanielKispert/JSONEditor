package com.daniel.jsoneditor.model.sessions;


/**
 * Result of a {@link FileSessionManager#openFile} call.
 * On success, {@link #sessionId()} is non-null and {@link #error()} is null.
 * On failure, {@link #sessionId()} is null and {@link #error()} contains a human-readable reason.
 */
public record OpenFileResult(String sessionId, String error)
{
    /** @return true when the file was opened successfully */
    public boolean success()
    {
        return sessionId != null;
    }
}
