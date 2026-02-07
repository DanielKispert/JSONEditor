package com.daniel.jsoneditor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for accessing application version information from version.properties.
 */
public final class VersionUtil
{
    private static final Logger logger = LoggerFactory.getLogger(VersionUtil.class);
    private static final String VERSION;
    
    static
    {
        String loadedVersion = "unknown";
        try (InputStream is = VersionUtil.class.getClassLoader().getResourceAsStream("version.properties"))
        {
            if (is != null)
            {
                final Properties props = new Properties();
                props.load(is);
                loadedVersion = props.getProperty("version", "unknown");
            }
            else
            {
                logger.warn("version.properties not found in classpath");
            }
        }
        catch (IOException e)
        {
            logger.error("Failed to load version from version.properties", e);
        }
        VERSION = loadedVersion;
    }
    
    private VersionUtil()
    {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * @return the application version (e.g., "0.16.1")
     */
    public static String getVersion()
    {
        return VERSION;
    }
}
