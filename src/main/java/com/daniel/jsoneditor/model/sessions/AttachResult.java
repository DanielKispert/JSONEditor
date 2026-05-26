package com.daniel.jsoneditor.model.sessions;

/**
 * Result of a {@link FileSessionManager#attachSession} call.
 * On success, {@link #sessionId()} is non-null and {@link #error()} is null.
 * On failure, {@link #sessionId()} is null and {@link #error()} contains a human-readable reason.
 */
public record AttachResult(String sessionId, String error)
{
    /** Compact constructor: enforces exactly one of sessionId / error is non-null. */
    public AttachResult
    {
        if ((sessionId == null) == (error == null))
        {
            throw new IllegalArgumentException(
                    "Exactly one of sessionId or error must be non-null; got sessionId=" + sessionId + ", error=" + error);
        }
    }

    public boolean success()
    {
        return sessionId != null;
    }

    public static AttachResult ofSuccess(final String id)
    {
        return new AttachResult(id, null);
    }

    public static AttachResult ofError(final String msg)
    {
        return new AttachResult(null, msg);
    }
}
