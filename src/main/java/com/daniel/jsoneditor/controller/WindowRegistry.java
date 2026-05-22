package com.daniel.jsoneditor.controller;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks which {@link AppWindow} is currently displaying which file (by canonical path).
 * Used by {@link FileOpenCoordinator} to prevent duplicate windows for the same file.
 */
public final class WindowRegistry
{
    private static final Logger logger = LoggerFactory.getLogger(WindowRegistry.class);

    private final Map<String, AppWindow> windowsByCanonicalPath = new ConcurrentHashMap<>();

    /** Registers the given window as the owner of {@code canonicalPath}. */
    public void register(final String canonicalPath, final AppWindow window)
    {
        windowsByCanonicalPath.put(canonicalPath, window);
    }

    /** Returns the window currently showing {@code canonicalPath}, or empty if none. */
    public Optional<AppWindow> findByPath(final String canonicalPath)
    {
        return Optional.ofNullable(windowsByCanonicalPath.get(canonicalPath));
    }

    /** Removes all entries pointing to the given window (used when a window closes). */
    public void unregisterWindow(final AppWindow window)
    {
        final boolean removed = windowsByCanonicalPath.values().removeIf(w -> w == window);
        if (removed)
        {
            logger.debug("Unregistered window from registry: {}", window);
        }
        else
        {
            logger.debug("unregisterWindow called but window was not found in registry: {}", window);
        }
    }
}
