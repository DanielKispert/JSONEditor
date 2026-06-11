package com.daniel.jsoneditor.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks open AppWindows by canonical file path. All methods must be called on the FX Application Thread.
 */
public class WindowRegistry
{
    private static final Logger logger = LoggerFactory.getLogger(WindowRegistry.class);
    private final Map<String, AppWindow> windowsByCanonicalPath = new HashMap<>();

    public synchronized void register(final String canonicalPath, final AppWindow window)
    {
        Objects.requireNonNull(canonicalPath, "canonicalPath must not be null");
        Objects.requireNonNull(window, "window must not be null");

        // Remove any prior registration of this window under a different path
        windowsByCanonicalPath.values().removeIf(w -> w == window);

        final AppWindow displaced = windowsByCanonicalPath.put(canonicalPath, window);
        if (displaced != null && displaced != window)
        {
            logger.warn("register() displaced existing window for path: {}", canonicalPath);
        }
    }

    /**
     * Finds a window for the given path. Returns empty if no window is registered
     * or if the registered window's stage is no longer showing (stale entry is removed).
     */
    public synchronized Optional<AppWindow> findByPath(final String canonicalPath)
    {
        final AppWindow window = windowsByCanonicalPath.get(canonicalPath);
        if (window == null)
        {
            return Optional.empty();
        }
        if (!window.isShowing())
        {
            logger.warn("Removing stale registry entry for path: {}", canonicalPath);
            windowsByCanonicalPath.remove(canonicalPath);
            return Optional.empty();
        }
        return Optional.of(window);
    }

    public synchronized void unregisterWindow(final AppWindow window)
    {
        windowsByCanonicalPath.values().removeIf(w -> w == window);
    }
}
