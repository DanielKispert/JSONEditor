package com.daniel.jsoneditor.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Unit tests for {@link SystemTrayManager}.
 * Tests requiring an actual system tray are guarded with {@code assumeTrue(SystemTray.isSupported())}
 * and will be skipped in headless CI environments.
 */
public class SystemTrayManagerTest
{
    private AppService mockAppService;
    private SystemTrayManager manager;

    @BeforeEach
    void setUp()
    {
        mockAppService = Mockito.mock(AppService.class);
        manager = new SystemTrayManager(mockAppService);
    }

    @AfterEach
    void tearDown()
    {
        // hide() has a null guard — safe to call unconditionally
        manager.hide();
    }

    /**
     * show() must return immediately without touching SystemTray when it is not supported.
     * Runs only in headless / no-tray environments (e.g. CI).
     */
    @Test
    void showIsNoOpWhenTrayNotSupported()
    {
        assumeFalse(SystemTray.isSupported(), "Skipped: runs only where SystemTray is not supported (headless/CI)");
        assertDoesNotThrow(() -> manager.show(8080));
        assertNull(getTrayIcon(manager), "trayIcon should remain null when SystemTray is not supported");
    }

    /**
     * Calling show() a second time must replace the existing icon, not add a duplicate.
     */
    @Test
    void showTwiceDoesNotCreateDuplicateIcons()
    {
        assumeTrue(SystemTray.isSupported(), "Skipped: SystemTray not supported on this platform");
        manager.show(8080);
        manager.show(9090);
        assertEquals(1, SystemTray.getSystemTray().getTrayIcons().length,
                "Exactly one icon should exist in the tray after two show() calls");
        assertNotNull(getTrayIcon(manager), "trayIcon field should be non-null");
    }

    /**
     * hide() must be a no-op when no icon has been shown yet.
     */
    @Test
    void hideIsSafeWhenNoIconExists()
    {
        assertNull(getTrayIcon(manager), "Precondition: no tray icon set");
        assertDoesNotThrow(() -> manager.hide());
        assertNull(getTrayIcon(manager), "trayIcon should remain null after hide() on empty state");
    }

    /**
     * hide() must remove the icon from the system tray and clear the trayIcon field.
     */
    @Test
    void hideRemovesIconWhenOneExists()
    {
        assumeTrue(SystemTray.isSupported(), "Skipped: SystemTray not supported on this platform");
        manager.show(8080);
        assertNotNull(getTrayIcon(manager), "Precondition: show() should set trayIcon");
        manager.hide();
        assertNull(getTrayIcon(manager), "trayIcon field should be null after hide()");
        assertEquals(0, SystemTray.getSystemTray().getTrayIcons().length,
                "System tray should have no icons after hide()");
    }

    /**
     * createIcon() must return an ARGB image with the exact requested dimensions.
     */
    @Test
    void createIconProducesImageOfRequestedSize() throws Exception
    {
        final int size = 32;
        final BufferedImage icon = invokeCreateIcon(size);
        assertNotNull(icon);
        assertEquals(size, icon.getWidth(), "Width should match requested size");
        assertEquals(size, icon.getHeight(), "Height should match requested size");
        assertEquals(BufferedImage.TYPE_INT_ARGB, icon.getType(), "Image type should be ARGB");
    }

    // --- reflection helpers ---

    private TrayIcon getTrayIcon(final SystemTrayManager mgr)
    {
        try
        {
            final Field field = SystemTrayManager.class.getDeclaredField("trayIcon");
            field.setAccessible(true);
            return (TrayIcon) field.get(mgr);
        }
        catch (final ReflectiveOperationException e)
        {
            throw new RuntimeException("Could not access trayIcon field", e);
        }
    }

    private static BufferedImage invokeCreateIcon(final int size) throws Exception
    {
        final Method method = SystemTrayManager.class.getDeclaredMethod("createIcon", int.class);
        method.setAccessible(true);
        return (BufferedImage) method.invoke(null, size);
    }
}
