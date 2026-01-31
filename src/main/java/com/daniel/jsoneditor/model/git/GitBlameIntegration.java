package com.daniel.jsoneditor.model.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * External tool integration for git blame functionality.
 * Combines GitBlameService and JsonPathToLineMapper to provide
 * blame information for JSON paths.
 */
public class GitBlameIntegration
{
    private static final Logger logger = LoggerFactory.getLogger(GitBlameIntegration.class);
    
    private final GitBlameService blameService = new GitBlameService();
    private final JsonPathToLineMapper lineMapper = new JsonPathToLineMapper();
    
    private String relativeFilePath;
    private boolean initialized = false;
    
    /**
     * Initialize with a JSON file.
     * Checks if file is in a git repository and builds path-to-line mapping.
     *
     * @param jsonFilePath absolute path to JSON file
     * @return true if successfully initialized
     */
    public boolean initialize(Path jsonFilePath)
    {
        close();
        
        if (!blameService.initialize(jsonFilePath))
        {
            logger.debug("Git blame not available for: {}", jsonFilePath);
            return false;
        }
        
        relativeFilePath = blameService.getRelativePath(jsonFilePath);
        if (relativeFilePath == null)
        {
            logger.warn("Could not determine relative path for: {}", jsonFilePath);
            close();
            return false;
        }
        
        lineMapper.buildMapping(jsonFilePath);
        initialized = true;
        logger.info("Git blame initialized for: {} ({})", jsonFilePath, relativeFilePath);
        return true;
    }
    
    /**
     * Get blame information for a JSON path.
     *
     * @param jsonPath JSON path (e.g., "/root/child/property")
     * @return blame info or null if not available
     */
    public GitBlameInfo getBlameForPath(String jsonPath)
    {
        if (!initialized)
        {
            return null;
        }
        
        final int lineNumber = lineMapper.getLineForPathOrParent(jsonPath);
        if (lineNumber < 0)
        {
            logger.debug("No line number found for path: {}", jsonPath);
            return null;
        }
        
        return blameService.getBlameForLine(relativeFilePath, lineNumber);
    }
    
    public boolean isAvailable()
    {
        return initialized;
    }
    
    /**
     * Refresh the line mapping after file changes.
     * Should be called after saving the file.
     */
    public void refresh(Path jsonFilePath)
    {
        if (initialized)
        {
            lineMapper.buildMapping(jsonFilePath);
            blameService.clearCache();
        }
    }
    
    public void close()
    {
        blameService.close();
        initialized = false;
        relativeFilePath = null;
    }
}
