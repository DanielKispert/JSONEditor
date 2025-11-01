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
            return generateSingleChangeMessage(commandPhase, changes.get(0));
        }
        else
        {
            return generateMultipleChangesMessage(commandPhase, changes);
        }
    }
    
    /**
     * Generates a message for a single model change.
     */
    private static String generateSingleChangeMessage(final String commandPhase, final ModelChange change)
    {
        final ChangeType type = change.getType();
        final String path = simplifyPath(change.getPath());
        
        switch (type)
        {
            case ADD:
                return String.format("%s: Added at %s", commandPhase, path);
            case REMOVE:
                return String.format("%s: Removed from %s", commandPhase, path);
            case REPLACE:
                return String.format("%s: Changed %s", commandPhase, path);
            case MOVE:
                return String.format("%s: Moved in %s (%d→%d)", commandPhase, path, change.getFromIndex(), change.getToIndex());
            case SORT:
                return String.format("%s: Sorted %s", commandPhase, path);
            default:
                return String.format("%s: %s at %s", commandPhase, type, path);
        }
    }
    
    /**
     * Generates a message for multiple model changes.
     */
    private static String generateMultipleChangesMessage(final String commandPhase, final List<ModelChange> changes)
    {
        final long addCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.ADD ? 1 : 0).sum();
        final long removeCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.REMOVE ? 1 : 0).sum();
        final long replaceCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.REPLACE ? 1 : 0).sum();
        final long moveCount = changes.stream().mapToLong(c -> c.getType() == ChangeType.MOVE ? 1 : 0).sum();
        
        final StringBuilder message = new StringBuilder(commandPhase).append(": ");
        
        if (addCount > 0) message.append(addCount).append(" added ");
        if (removeCount > 0) message.append(removeCount).append(" removed ");
        if (replaceCount > 0) message.append(replaceCount).append(" changed ");
        if (moveCount > 0) message.append(moveCount).append(" moved ");
        
        return message.toString().trim();
    }
    
    /**
     * Simplifies JSON paths for display in toasts by showing only the last few segments.
     */
    private static String simplifyPath(final String path)
    {
        if (path == null || path.isEmpty())
        {
            return "root";
        }
        
        // Remove leading slash and split by slash
        final String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        final String[] segments = cleanPath.split("/");
        
        if (segments.length <= 2)
        {
            return cleanPath.isEmpty() ? "root" : cleanPath;
        }
        
        // Show last 2 segments for readability
        return ".../" + segments[segments.length - 2] + "/" + segments[segments.length - 1];
    }
}



