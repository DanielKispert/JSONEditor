package com.daniel.jsoneditor.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WindowRegistryTest
{
    @Test
    void windowRegistry_fullLifecycle()
    {
        final WindowRegistry registry = new WindowRegistry();
        final AppWindow windowA = mock(AppWindow.class);
        final AppWindow windowB = mock(AppWindow.class);

        // Register and look up a single entry
        assertFalse(registry.findByPath("/foo/bar.json").isPresent(), "lookup before register must return empty");
        registry.register("/foo/bar.json", windowA);
        assertTrue(registry.findByPath("/foo/bar.json").isPresent(), "lookup after register must succeed");
        assertSame(windowA, registry.findByPath("/foo/bar.json").get(), "findByPath must return the registered window");

        // Simulate same window re-registering at new paths (stale entry eviction)
        registry.register("/files/a.json", windowA);
        registry.register("/files/b.json", windowA);
        assertFalse(registry.findByPath("/files/a.json").isPresent(),
                "Old path must be evicted when the same window re-registers at a new path");
        assertTrue(registry.findByPath("/files/b.json").isPresent(), "New path must be registered");

        // Register multiple paths for windowA and one for windowB
        registry.register("/path/a.json", windowA);
        registry.register("/path/b.json", windowA);
        registry.register("/path/c.json", windowB);

        // Unregistering windowA must remove all its paths but leave windowB's paths intact
        registry.unregisterWindow(windowA);
        assertFalse(registry.findByPath("/path/a.json").isPresent(), "/path/a.json must be gone after unregisterWindow(windowA)");
        assertFalse(registry.findByPath("/path/b.json").isPresent(), "/path/b.json must be gone after unregisterWindow(windowA)");
        assertFalse(registry.findByPath("/foo/bar.json").isPresent(), "/foo/bar.json must be gone after unregisterWindow(windowA)");
        assertTrue(registry.findByPath("/path/c.json").isPresent(), "/path/c.json (windowB) must survive unregisterWindow(windowA)");
    }
}
