package com.daniel.jsoneditor.controller.impl.commands;

import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.Command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


/**
 * Manages command execution history and performs undo/redo based on immutable ModelChange entries.
 * Applies changes through the public WritableModel API (no internal shortcuts) to keep semantics consistent.
 */
public class CommandManager
{
    private final Deque<HistoryEntry> undoStack = new ArrayDeque<>();
    
    private final Deque<HistoryEntry> redoStack = new ArrayDeque<>();
    
    private final WritableModel model;
    
    /**
     * @param model
     *         writable model used to apply and revert changes
     */
    public CommandManager(WritableModel model)
    {
        this.model = model;
    }
    
    /**
     * Executes a command and records its resulting changes on the undo stack.
     *
     * @param cmd
     *         command to execute
     *
     * @return list of produced changes (empty if no state mutation)
     */
    public List<ModelChange> executeCommand(Command cmd)
    {
        final List<ModelChange> changes = safeList(cmd.execute());
        if (!changes.isEmpty())
        {
            undoStack.push(new HistoryEntry(cmd, changes));
            redoStack.clear();
        }
        return changes;
    }
    
    /**
     * Reverts the last executed command if present.
     *
     * @return list of applied inverse changes (empty if nothing to undo)
     */
    public List<ModelChange> undo()
    {
        if (undoStack.isEmpty())
        {
            return List.of();
        }
        final HistoryEntry entry = undoStack.pop();
        final List<ModelChange> inverted = invert(entry.getChanges());
        apply(inverted);
        redoStack.push(entry);
        return inverted;
    }
    
    /**
     * Re-applies the last undone command if present.
     *
     * @return list of re-applied original changes (empty if nothing to redo)
     */
    public List<ModelChange> redo()
    {
        if (redoStack.isEmpty())
        {
            return List.of();
        }
        final HistoryEntry entry = redoStack.pop();
        apply(entry.getChanges());
        undoStack.push(entry);
        return entry.getChanges();
    }
    
    private void apply(List<ModelChange> changes)
    {
        for (final ModelChange c : changes)
        {
            switch (c.getType())
            {
                case ADD:
                    model.setNode(c.getPath(), c.getNewValue());
                    break;
                case REMOVE:
                    model.removeNode(c.getPath());
                    break;
                case REPLACE:
                    model.setNode(c.getPath(), c.getNewValue());
                    break;
                case MOVE:
                    // TODO move not yet implemented
                    break;
                case SETTINGS_CHANGED:
                    // TODO settings changes not yet implemented
                    break;
            }
        }
    }
    
    private List<ModelChange> invert(List<ModelChange> original)
    {
        final List<ModelChange> out = new ArrayList<>(original.size());
        for (int i = original.size() - 1; i >= 0; i--)
        {
            final ModelChange c = original.get(i);
            switch (c.getType())
            {
                case ADD:
                    out.add(ModelChange.remove(c.getPath(), c.getNewValue()));
                    break;
                case REMOVE:
                    out.add(ModelChange.add(c.getPath(), c.getOldValue()));
                    break;
                case REPLACE:
                    out.add(ModelChange.replace(c.getPath(), c.getNewValue(), c.getOldValue()));
                    break;
                case MOVE:
                    out.add(ModelChange.move(c.getPath(), c.getToIndex(), c.getFromIndex()));
                    break;
                case SETTINGS_CHANGED:
                    out.add(ModelChange.settingsChanged(c.getNewValue(), c.getOldValue()));
                    break;
            }
        }
        return out;
    }
    
    private List<ModelChange> safeList(List<ModelChange> list)
    {
        return list == null ? List.of() : list;
    }
    
    private static final class HistoryEntry
    {
        private final Command command;
        
        private final List<ModelChange> changes;
        
        HistoryEntry(Command command, List<ModelChange> changes)
        {
            this.command = command;
            this.changes = changes;
        }
        
        Command getCommand()
        {
            return command;
        }
        
        List<ModelChange> getChanges()
        {
            return changes;
        }
    }
}
