package com.daniel.jsoneditor.controller;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which {@link AppWindow} is currently displaying which file (by canonical path).
 * Used by {@link FileOpenCoordinator} to prevent duplicate windows for the same file.
 */
public final class WindowRegistry
{
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
        windowsByCanonicalPath.values().removeIf(w -> w == window);
    }
}
