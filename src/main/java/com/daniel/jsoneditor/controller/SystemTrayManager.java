package com.daniel.jsoneditor.controller;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * Manages the system tray icon for platforms that support it (Windows, some Linux DEs).
 * Skipped on macOS (which uses the dock icon) and when {@link SystemTray} is not available.
 */
public class SystemTrayManager
{
    private static final Logger logger = LoggerFactory.getLogger(SystemTrayManager.class);

    private final AppService appService;
    private TrayIcon trayIcon;

    public SystemTrayManager(final AppService appService)
    {
        this.appService = appService;
    }

    /**
     * Adds the system tray icon with a popup menu.
     * No-op on macOS or platforms where {@link SystemTray} is not supported.
     *
     * @param port the MCP server port shown in the tooltip
     */
    public void show(final int port)
    {
        if (isMacOs())
        {
            logger.debug("Skipping system tray on macOS");
            return;
        }
        if (!SystemTray.isSupported())
        {
            logger.info("System tray not supported on this platform — skipping tray icon");
            return;
        }

        final PopupMenu popup = new PopupMenu();

        final MenuItem newWindowItem = new MenuItem("New Window");
        newWindowItem.addActionListener(e -> Platform.runLater(appService::createWindow));

        final MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(e -> Platform.runLater(() ->
        {
            appService.shutdown();
            Platform.exit();
        }));

        popup.add(newWindowItem);
        popup.addSeparator();
        popup.add(quitItem);

        trayIcon = new TrayIcon(createIcon(), "JSON Editor (MCP Server running on port " + port + ")", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> Platform.runLater(appService::createWindow));

        try
        {
            SystemTray.getSystemTray().add(trayIcon);
            logger.info("System tray icon added (port {})", port);
        }
        catch (final AWTException ex)
        {
            logger.error("Failed to add system tray icon", ex);
        }
    }

    /**
     * Removes the tray icon. Safe to call when no icon is currently showing.
     */
    public void hide()
    {
        if (trayIcon != null)
        {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            logger.info("System tray icon removed");
        }
    }

    /**
     * Creates a simple 16x16 icon: a rounded blue rectangle with a white "J" letter.
     */
    private static BufferedImage createIcon()
    {
        final int size = 16;
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(30, 100, 200));
        g.fillRoundRect(0, 0, size, size, 4, 4);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        final FontMetrics fm = g.getFontMetrics();
        final int x = (size - fm.stringWidth("J")) / 2;
        final int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString("J", x, y);

        g.dispose();
        return image;
    }

    private static boolean isMacOs()
    {
        return System.getProperty("os.name", "").contains("Mac");
    }
}
