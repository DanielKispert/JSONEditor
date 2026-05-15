package com.daniel.jsoneditor.controller;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Color;
import java.awt.Dimension;
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
 * Skipped on platforms where {@link SystemTray} is not available.
 */
public class SystemTrayManager
{
    private static final Logger logger = LoggerFactory.getLogger(SystemTrayManager.class);

    private static final Color ICON_BACKGROUND = new Color(30, 100, 200);
    private static final int ICON_ARC = 4;
    private static final int ICON_FONT_SIZE = 11;
    private static final String ICON_LETTER = "J";

    private final AppService appService;
    private volatile TrayIcon trayIcon;

    public SystemTrayManager(final AppService appService)
    {
        this.appService = appService;
    }

    /**
     * Adds the system tray icon with a popup menu.
     * No-op on platforms where {@link SystemTray} is not supported.
     *
     * @param port the MCP server port shown in the tooltip
     */
    public synchronized void show(final int port)
    {
        try
        {
            if (!SystemTray.isSupported())
            {
                logger.info("System tray not supported on this platform — skipping tray icon");
                return;
            }
        }
        catch (final HeadlessException ex)
        {
            logger.info("Headless environment detected — skipping tray icon");
            return;
        }
        if (trayIcon != null)
        {
            hide();
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

        try
        {
            final Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
            final int iconSize = Math.max(traySize.width, traySize.height);

            trayIcon = new TrayIcon(createIcon(iconSize), "JSON Editor (MCP Server running on port " + port + ")", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(appService::createWindow));

            SystemTray.getSystemTray().add(trayIcon);
            logger.info("System tray icon added (port {})", port);
        }
        catch (final HeadlessException ex)
        {
            logger.info("Headless environment detected — skipping tray icon");
            trayIcon = null;
        }
        catch (final AWTException ex)
        {
            logger.error("Failed to add system tray icon", ex);
        }
    }

    /**
     * Removes the tray icon. Safe to call when no icon is currently showing.
     */
    public synchronized void hide()
    {
        if (trayIcon != null)
        {
            try
            {
                SystemTray.getSystemTray().remove(trayIcon);
            }
            catch (final HeadlessException ex)
            {
                logger.info("Headless environment — cannot remove tray icon from system tray");
            }
            trayIcon = null;
            logger.info("System tray icon removed");
        }
    }

    /**
     * Creates a rounded blue rectangle icon with a white "J" letter, sized for the system tray.
     */
    private static BufferedImage createIcon(final int size)
    {
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(ICON_BACKGROUND);
        g.fillRoundRect(0, 0, size, size, ICON_ARC, ICON_ARC);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (size * ICON_FONT_SIZE / 16.0)));
        final FontMetrics fm = g.getFontMetrics();
        final int x = (size - fm.stringWidth(ICON_LETTER)) / 2;
        final int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(ICON_LETTER, x, y);

        g.dispose();
        return image;
    }
}
