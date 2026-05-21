package com.daniel.jsoneditor.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for safe path canonicalization — falls back to the absolute path when
 * {@link File#getCanonicalPath()} fails, logging a warning instead of throwing.
 */
public final class CanonicalPaths
{
    private static final Logger logger = LoggerFactory.getLogger(CanonicalPaths.class);

    private CanonicalPaths() {}

    /**
     * Returns the canonical path for {@code file}, falling back to the absolute path
     * when canonicalization fails (e.g. file does not exist yet, IO error).
     * Logs a warning on fallback so callers do not need to.
     *
     * @param file the file to canonicalize, must not be {@code null}
     * @return canonical path, or absolute path as fallback
     */
    public static String canonicalize(final File file)
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (final IOException e)
        {
            logger.warn("Cannot canonicalize {}, falling back to absolute path", file, e);
            return file.getAbsolutePath();
        }
    }
}
