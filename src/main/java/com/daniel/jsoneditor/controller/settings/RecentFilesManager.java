package com.daniel.jsoneditor.controller.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Tracks recently opened JSON+schema file pairs and persists them across sessions.
 * Stores up to {@value #MAX_RECENT_FILES} entries in a dedicated properties file.
 */
public class RecentFilesManager
{
    private static final Logger logger = LoggerFactory.getLogger(RecentFilesManager.class);

    private static final File RECENT_FILES_PATH = new File(
            System.getProperty("user.home") + "/.jsoneditor/recent.properties");

    private static final int MAX_RECENT_FILES = 10;

    private static final String KEY_JSON = "recent_%d_json";

    private static final String KEY_SCHEMA = "recent_%d_schema";

    /** A JSON+schema file pair. */
    public record RecentFile(File jsonFile, File schemaFile) {}

    private final List<RecentFile> recentFiles;

    private final List<Runnable> changeListeners;
    private final Object lock = new Object();

    public RecentFilesManager()
    {
        this.recentFiles = new ArrayList<>();
        this.changeListeners = new ArrayList<>();
        load();
    }

    /**
     * Adds a file pair to the top of the recent list.
     * Removes any existing entry for the same JSON file first.
     * Trims to {@value #MAX_RECENT_FILES} entries and persists.
     */
    public void addRecentFile(final File jsonFile, final File schemaFile)
    {
        if (jsonFile == null || schemaFile == null)
        {
            return;
        }
        final List<Runnable> listeners;
        synchronized (lock)
        {
            recentFiles.removeIf((final RecentFile rf) -> rf.jsonFile().equals(jsonFile));
            recentFiles.add(0, new RecentFile(jsonFile, schemaFile));
            while (recentFiles.size() > MAX_RECENT_FILES)
            {
                recentFiles.remove(recentFiles.size() - 1);
            }
            save();
            listeners = new ArrayList<>(changeListeners);
        }
        for (final Runnable listener : listeners)
        {
            listener.run();
        }
    }

    /** Returns a snapshot of recent files, newest first. */
    public List<RecentFile> getRecentFiles()
    {
        synchronized (lock)
        {
            return new ArrayList<>(recentFiles);
        }
    }

    /** Clears all recent files and persists the empty list. */
    public void clear()
    {
        final List<Runnable> listeners;
        synchronized (lock)
        {
            recentFiles.clear();
            save();
            listeners = new ArrayList<>(changeListeners);
        }
        for (final Runnable listener : listeners)
        {
            listener.run();
        }
    }

    /**
     * Registers a listener called whenever the recent files list changes.
     * The listener is invoked on whichever thread triggered the change.
     */
    public void addChangeListener(final Runnable listener)
    {
        synchronized (lock)
        {
            changeListeners.add(listener);
        }
    }

    private void load()
    {
        final Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(RECENT_FILES_PATH))
        {
            props.load(in);
        }
        catch (FileNotFoundException e)
        {
            return; // no file yet — start with empty list
        }
        catch (IOException e)
        {
            logger.error("Could not load recent files from {}", RECENT_FILES_PATH, e);
            return;
        }
        for (int i = 0; i < MAX_RECENT_FILES; i++)
        {
            final String jsonPath = props.getProperty(String.format(KEY_JSON, i));
            final String schemaPath = props.getProperty(String.format(KEY_SCHEMA, i));
            if (jsonPath == null || schemaPath == null)
            {
                break;
            }
            final File jsonFile = new File(jsonPath);
            final File schemaFile = new File(schemaPath);
            if (jsonFile.exists() && schemaFile.exists())
            {
                recentFiles.add(new RecentFile(jsonFile, schemaFile));
            }
        }
    }

    private void save()
    {
        final File dir = RECENT_FILES_PATH.getParentFile();
        if (dir != null && !dir.exists())
        {
            if (!dir.mkdirs() && !dir.exists())
            {
                logger.error("Cannot create directory for recent files: {}", dir);
                return;
            }
        }
        final Properties props = new Properties();
        for (int i = 0; i < recentFiles.size(); i++)
        {
            final RecentFile rf = recentFiles.get(i);
            props.setProperty(String.format(KEY_JSON, i), rf.jsonFile().getAbsolutePath());
            props.setProperty(String.format(KEY_SCHEMA, i), rf.schemaFile().getAbsolutePath());
        }
        final File tempFile = new File(RECENT_FILES_PATH.getParentFile(), RECENT_FILES_PATH.getName() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tempFile))
        {
            props.store(out, null);
        }
        catch (IOException e)
        {
            logger.error("Could not save recent files to {}", RECENT_FILES_PATH, e);
            tempFile.delete();
            return;
        }
        try
        {
            Files.move(tempFile.toPath(), RECENT_FILES_PATH.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e)
        {
            // ATOMIC_MOVE not supported on this filesystem — fall back to direct write
            logger.warn("Atomic rename failed, falling back to direct write: {}", e.getMessage());
            try (FileOutputStream out = new FileOutputStream(RECENT_FILES_PATH))
            {
                props.store(out, null);
            }
            catch (IOException ex)
            {
                logger.error("Could not save recent files to {}", RECENT_FILES_PATH, ex);
            }
        }
    }
}
