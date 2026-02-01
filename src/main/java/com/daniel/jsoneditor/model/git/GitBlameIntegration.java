package com.daniel.jsoneditor.model.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Map<String, GitBlameInfo> pathCache = new HashMap<>();
    
    private String relativeFilePath;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean initializing = new AtomicBoolean(false);
    
    /**
     * Initialize with a JSON file asynchronously.
     * Returns immediately, loading happens in background thread.
     *
     * @param jsonFilePath absolute path to JSON file
     * @return CompletableFuture that completes when initialization is done
     */
    public CompletableFuture<Void> initialize(Path jsonFilePath)
    {
        close();
        initializing.set(true);
        
        return CompletableFuture.runAsync(() -> {
            try
            {
                if (!blameService.initialize(jsonFilePath))
                {
                    logger.debug("Git blame not available for: {}", jsonFilePath);
                    return;
                }
                
                relativeFilePath = blameService.getRelativePath(jsonFilePath);
                if (relativeFilePath == null)
                {
                    logger.warn("Could not determine relative path for: {}", jsonFilePath);
                    close();
                    return;
                }
                
                lineMapper.buildMapping(jsonFilePath);
                synchronized (pathCache)
                {
                    pathCache.clear();
                }
                initialized.set(true);
                logger.info("Git blame initialized for: {} ({})", jsonFilePath, relativeFilePath);
            }
            finally
            {
                initializing.set(false);
            }
        });
    }
    
    /**
     * Get blame information for a JSON path.
     *
     * @param jsonPath JSON path (e.g., "/root/child/property")
     * @return blame info or null if not available or still loading
     */
    public GitBlameInfo getBlameForPath(String jsonPath)
    {
        if (!initialized.get())
        {
            return null;
        }
        
        synchronized (pathCache)
        {
            if (pathCache.containsKey(jsonPath))
            {
                return pathCache.get(jsonPath);
            }
        }
        
        final int lineNumber = lineMapper.getLineForPathOrParent(jsonPath);
        if (lineNumber < 0)
        {
            logger.debug("No line number found for path: {}", jsonPath);
            synchronized (pathCache)
            {
                pathCache.put(jsonPath, null);
            }
            return null;
        }
        
        final GitBlameInfo blameInfo = blameService.getBlameForLine(relativeFilePath, lineNumber);
        synchronized (pathCache)
        {
            pathCache.put(jsonPath, blameInfo);
        }
        return blameInfo;
    }
    
    public boolean isAvailable()
    {
        return initialized.get() || initializing.get();
    }
    
    public boolean isLoading()
    {
        return initializing.get();
    }
    
    public void close()
    {
        blameService.close();
        synchronized (pathCache)
        {
            pathCache.clear();
        }
        initialized.set(false);
        initializing.set(false);
        relativeFilePath = null;
    }
}
