package com.daniel.jsoneditor.controller.impl.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.changes.ModelChange;
import com.daniel.jsoneditor.model.commands.Command;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of CommandManager that manages command execution history and performs undo/redo
 * based on immutable ModelChange entries. Applies changes through the public WritableModel API
 * (no internal shortcuts) to keep semantics consistent.
 */
public class CommandManagerImpl implements CommandManager
{
    private static final Logger logger = LoggerFactory.getLogger(CommandManagerImpl.class);
    
    private final Deque<HistoryEntry> undoStack = new ArrayDeque<>();
    
    private final Deque<HistoryEntry> redoStack = new ArrayDeque<>();
    
    private final WritableModel model;
    
    private int savedStateIndex = 0; // Index in undo stack where the last save occurred
    
    private UnsavedChangesCallback unsavedChangesCallback;
    
    /**
     * @param model
     *         writable model used to apply and revert changes
     */
    public CommandManagerImpl(WritableModel model)
    {
        this.model = model;
    }
    
    @Override
    public void setUnsavedChangesCallback(final UnsavedChangesCallback callback)
    {
        this.unsavedChangesCallback = callback;
    }
    
    /**
     * Notifies the callback about unsaved changes count if callback is set.
     */
    private void notifyUnsavedChangesCount()
    {
        if (unsavedChangesCallback != null)
        {
            unsavedChangesCallback.onUnsavedChangesCountChanged(getUnsavedChangesCount());
        }
    }
    
    @Override
    public List<ModelChange> executeCommand(final Command cmd)
    {
        logger.debug("Executing command: {}", cmd.getLabel());
        final List<ModelChange> changes = safeList(cmd.execute());
        for (final ModelChange change : changes)
        {
            logger.debug("Model Change executed: {}", change);
        }
        if (!changes.isEmpty() && cmd.isUndoable())
        {
            undoStack.push(new HistoryEntry(cmd, changes));
            redoStack.clear();
            notifyUnsavedChangesCount(); // Notify about changes count update
        }
        // notify UI regardless of undoability when something happened
        if (!changes.isEmpty())
        {
            model.sendEvent(new Event(EventEnum.COMMAND_APPLIED, cmd, "EXECUTE", changes));
        }
        return changes;
    }
    
    @Override
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
        notifyUnsavedChangesCount(); // Notify about changes count update
        if (!inverted.isEmpty())
        {
            model.sendEvent(new Event(EventEnum.COMMAND_APPLIED, entry.getCommand(), "UNDO", inverted));
        }
        return inverted; // return inverse (REMOVE for previous ADD)
    }
    
    @Override
    public List<ModelChange> redo()
    {
        if (redoStack.isEmpty())
        {
            return List.of();
        }
        final HistoryEntry entry = redoStack.pop();
        apply(entry.getChanges());
        undoStack.push(entry);
        notifyUnsavedChangesCount(); // Notify about changes count update
        if (!entry.getChanges().isEmpty())
        {
            model.sendEvent(new Event(EventEnum.COMMAND_APPLIED, entry.getCommand(), "REDO", entry.getChanges()));
        }
        return entry.getChanges();
    }
    
    private void apply(List<ModelChange> changes)
    {
        for (final ModelChange c : changes)
        {
            switch (c.getType())
            {
                case ADD:
                case REPLACE:
                    model.setNode(c.getPath(), c.getNewValue());
                    break;
                case REMOVE:
                    model.removeNodes(List.of(c.getPath()));
                    break;
                case MOVE:
                    applyMove(c);
                    break;
                case SETTINGS_CHANGED:
                    // TODO settings changes not yet implemented
                    break;
                case SORT:
                    applySort(c);
                    break;
            }
        }
    }
    
    private void applyMove(ModelChange c)
    {
        // path points to parent array
        JsonNode parent = model.getNodeForPath(c.getPath()).getNode();
        if (parent != null && parent.isArray())
        {
            ArrayNode array = (ArrayNode) parent;
            int from = c.getFromIndex();
            int to = c.getToIndex();
            if (from >= 0 && from < array.size() && to >= 0 && to < array.size())
            {
                JsonNode node = array.remove(from);
                if (to > from)
                {
                    to = to - 1; // adjust after removal
                }
                array.insert(to, node);
            }
        }
    }
    
    private void applySort(ModelChange c)
    {
        JsonNode parent = model.getNodeForPath(c.getPath()).getNode();
        if (parent != null && parent.isArray())
        {
            ArrayNode array = (ArrayNode) parent;
            array.removeAll();
            ArrayNode newOrder = (ArrayNode) c.getNewValue();
            // copy elements
            for (JsonNode n : newOrder)
            {
                array.add(n.deepCopy());
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
                case SORT:
                    out.add(ModelChange.sort(c.getPath(), c.getNewValue(), c.getOldValue()));
                    break;
            }
        }
        return out;
    }
    
    private List<ModelChange> safeList(List<ModelChange> list)
    {
        return list == null ? List.of() : list;
    }
    
    @Override
    public void clearHistory()
    {
        undoStack.clear();
        redoStack.clear();
        savedStateIndex = 0; // Reset saved state when clearing history
        notifyUnsavedChangesCount(); // Notify about changes count update
    }
    
    @Override
    public void markAsSaved()
    {
        savedStateIndex = undoStack.size();
        notifyUnsavedChangesCount(); // Notify about changes count update
    }
    
    private int getUnsavedChangesCount()
    {
        return Math.abs(undoStack.size() - savedStateIndex);
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
