package com.daniel.jsoneditor.model.git;

import java.time.Instant;

public class GitBlameInfo
{
    private final String authorName;
    private final String authorEmail;
    private final String commitHash;
    private final Instant commitTime;
    private final String commitMessage;
    
    public GitBlameInfo(String authorName, String authorEmail, String commitHash, Instant commitTime, String commitMessage)
    {
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.commitHash = commitHash;
        this.commitTime = commitTime;
        this.commitMessage = commitMessage;
    }
    
    public String getAuthorName()
    {
        return authorName;
    }
    
    public String getAuthorEmail()
    {
        return authorEmail;
    }
    
    public String getCommitHash()
    {
        return commitHash;
    }
    
    public String getShortCommitHash()
    {
        return commitHash != null && commitHash.length() > 7 ? commitHash.substring(0, 7) : commitHash;
    }
    
    public Instant getCommitTime()
    {
        return commitTime;
    }
    
    public String getCommitMessage()
    {
        return commitMessage;
    }
    
    public String getShortCommitMessage()
    {
        if (commitMessage == null) return "";
        final int newlineIndex = commitMessage.indexOf('\n');
        return newlineIndex > 0 ? commitMessage.substring(0, newlineIndex) : commitMessage;
    }
    
    /**
     * Get a color derived from the commit hash for visual distinction.
     * Returns a web color string like "#A3B5C7"
     */
    public String getCommitColor()
    {
        if (commitHash == null || commitHash.isEmpty())
        {
            return "#808080";
        }
        
        final int hash = commitHash.hashCode();
        final int r = (hash & 0xFF0000) >> 16;
        final int g = (hash & 0x00FF00) >> 8;
        final int b = hash & 0x0000FF;
        
        final int minBrightness = 80;
        final int maxBrightness = 200;
        final int adjustedR = minBrightness + (r * (maxBrightness - minBrightness) / 255);
        final int adjustedG = minBrightness + (g * (maxBrightness - minBrightness) / 255);
        final int adjustedB = minBrightness + (b * (maxBrightness - minBrightness) / 255);
        
        return String.format("#%02X%02X%02X", adjustedR, adjustedG, adjustedB);
    }
    
    @Override
    public String toString()
    {
        return String.format("%s (%s)", authorName, getShortCommitHash());
    }
}
