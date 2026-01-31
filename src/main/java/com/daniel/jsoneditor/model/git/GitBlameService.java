package com.daniel.jsoneditor.model.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for retrieving git blame information for JSON file lines.
 * Provides information about who last modified each line in a file.
 */
public class GitBlameService
{
    private static final Logger logger = LoggerFactory.getLogger(GitBlameService.class);
    
    private Repository repository;
    private Git git;
    private final Map<String, BlameResult> blameCache = new HashMap<>();
    
    /**
     * Initialize the service with a JSON file path.
     * Attempts to find the git repository containing the file.
     *
     * @param jsonFilePath path to the JSON file
     * @return true if git repository was found and initialized
     */
    public boolean initialize(Path jsonFilePath)
    {
        close();
        
        try
        {
            final File file = jsonFilePath.toFile();
            final FileRepositoryBuilder builder = new FileRepositoryBuilder();
            repository = builder
                .findGitDir(file)
                .setMustExist(true)
                .build();
            
            git = new Git(repository);
            logger.info("Git repository found: {}", repository.getDirectory());
            return true;
        }
        catch (IOException e)
        {
            logger.debug("No git repository found for file: {}", jsonFilePath);
            return false;
        }
    }
    
    /**
     * Get blame information for a specific line in a file.
     *
     * @param relativeFilePath path relative to git repository root
     * @param lineNumber 0-based line number
     * @return blame info or null if not available
     */
    public GitBlameInfo getBlameForLine(String relativeFilePath, int lineNumber)
    {
        if (git == null || repository == null)
        {
            return null;
        }
        
        try
        {
            final BlameResult blameResult = getOrComputeBlame(relativeFilePath);
            if (blameResult == null)
            {
                return null;
            }
            
            final RevCommit commit = blameResult.getSourceCommit(lineNumber);
            if (commit == null)
            {
                return null;
            }
            
            final PersonIdent author = blameResult.getSourceAuthor(lineNumber);
            final Instant commitTime = Instant.ofEpochSecond(commit.getCommitTime());
            
            return new GitBlameInfo(
                author.getName(),
                author.getEmailAddress(),
                commit.getName(),
                commitTime,
                commit.getFullMessage()
            );
        }
        catch (Exception e)
        {
            logger.error("Error getting blame info for {}:{}", relativeFilePath, lineNumber, e);
            return null;
        }
    }
    
    private BlameResult getOrComputeBlame(String relativeFilePath) throws GitAPIException
    {
        if (!blameCache.containsKey(relativeFilePath))
        {
            logger.debug("Computing blame for file: {}", relativeFilePath);
            final BlameResult result = git.blame()
                .setFilePath(relativeFilePath)
                .call();
            blameCache.put(relativeFilePath, result);
        }
        return blameCache.get(relativeFilePath);
    }
    
    /**
     * Get the relative path of a file from the repository root.
     *
     * @param absolutePath absolute file path
     * @return relative path or null if file is not in repository
     */
    public String getRelativePath(Path absolutePath)
    {
        if (repository == null)
        {
            return null;
        }
        
        final Path workTree = repository.getWorkTree().toPath();
        if (absolutePath.startsWith(workTree))
        {
            return workTree.relativize(absolutePath).toString().replace('\\', '/');
        }
        return null;
    }
    
    public boolean isInitialized()
    {
        return git != null && repository != null;
    }
    
    /**
     * Clear the blame cache. Should be called when file content changes.
     */
    public void clearCache()
    {
        blameCache.clear();
    }
    
    /**
     * Close the git repository and release resources.
     */
    public void close()
    {
        blameCache.clear();
        if (git != null)
        {
            git.close();
            git = null;
        }
        if (repository != null)
        {
            repository.close();
            repository = null;
        }
    }
}
