package com.daniel.jsoneditor.view.impl.jfx.toast;

import com.daniel.jsoneditor.model.changes.ChangeType;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.statemachine.impl.Event;

import java.util.List;


/**
 * Utility class for generating descriptive debug toast messages based on model changes.
 */
public final class DebugToastMessageGenerator
{
    private DebugToastMessageGenerator()
    {
        // Utility class
    }
    
    /**
     * Generates a concise debug message describing the command and its changes.
     *
     * @param event the event containing command and change information
     * @return a formatted debug message suitable for toast display
     */
    public static String generateMessage(final Event event)
    {
        final String commandLabel = event.getCommandLabel();
        final String commandPhase = event.getCommandPhase();
        final List<ModelChange> changes = event.getChanges();
        
        if (changes == null || changes.isEmpty())
        {
            return String.format("%s (%s)", commandLabel != null ? commandLabel : "Command", commandPhase);
        }
        
        if (changes.size() == 1)
        {
            return generateSingleChangeMessage(changes.get(0));
        }
        else
        {
            return generateMultipleChangesMessage(changes);
        }
    }
    
    /**
     * Generates a message for a single model change.
     */
    private static String generateSingleChangeMessage(final ModelChange change)
    {
        final ChangeType type = change.getType();
        final String path = simplifyPath(change.getPath());
        
        switch (type)
        {
            case ADD:
                final String addedValue = shortenValue(change.getNewValue());
                return String.format("➕ %s = %s", path, addedValue);
            case REMOVE:
                final String removedValue = shortenValue(change.getOldValue());
                return String.format("➖ %s = %s", path, removedValue);
            case REPLACE:
                final String oldVal = shortenValue(change.getOldValue());
                final String newVal = shortenValue(change.getNewValue());
                return String.format("✏️ %s: %s→%s", path, oldVal, newVal);
            case MOVE:
                return String.format("↕️ %s (%d→%d)", path, change.getFromIndex(), change.getToIndex());
            case SORT:
                return String.format("🔄 %s", path);
            default:
                return String.format("%s %s", type, path);
        }
    }
    
    /**
     * Generates a message for multiple model changes.
     */
    private static String generateMultipleChangesMessage(final List<ModelChange> changes)
    {
        final long addCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.ADD ? 1 : 0).sum();
        final long removeCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.REMOVE ? 1 : 0).sum();
        final long replaceCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.REPLACE ? 1 : 0).sum();
        final long moveCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.MOVE ? 1 : 0).sum();
        
        final StringBuilder message = new StringBuilder();
        
        if (addCount > 0) message.append("➕").append(addCount).append(" ");
        if (removeCount > 0) message.append("➖").append(removeCount).append(" ");
        if (replaceCount > 0) message.append("✏️").append(replaceCount).append(" ");
        if (moveCount > 0) message.append("↕️").append(moveCount).append(" ");
        
        return message.toString().trim();
    }
    
    /**
     * Simplifies JSON paths for display in toasts by showing the last 3 segments.
     */
    private static String simplifyPath(final String path)
    {
        if (path == null || path.isEmpty())
        {
            return "/";
        }
        
        final String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        if (cleanPath.isEmpty())
        {
            return "/";
        }
        
        final String[] segments = cleanPath.split("/");
        
        if (segments.length <= 3)
        {
            return cleanPath;
        }
        
        return "…/" + segments[segments.length - 3] + "/" + segments[segments.length - 2] + "/" + segments[segments.length - 1];
    }
    
    /**
     * Shortens a value for display in toasts. Limits length and handles different types.
     */
    private static String shortenValue(final Object value)
    {
        if (value == null)
        {
            return "null";
        }
        
        final String str = value.toString();
        final int maxLength = 30;
        
        if (str.length() <= maxLength)
        {
            return str;
        }
        
        return str.substring(0, maxLength) + "…";
    }
}



