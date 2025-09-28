package com.daniel.jsoneditor.model.commands;

import com.daniel.jsoneditor.model.changes.ModelChange;
import java.util.List;

/**
 * A command mutates the model and returns a list of semantic model changes for UI + Undo/Redo.
 */
public interface Command {
    /**
     * Executes the command and returns the list of changes (empty if no mutation happened).
     * @return list of ModelChange (never null)
     */
    List<ModelChange> execute();
    
    /**
     * @return human readable label for UI / debugging.
     */
    default String getLabel() { return getClass().getSimpleName(); }
    
    /**
     * @return category grouping (STRUCTURE, VALUE, REFERENCE, SETTINGS, OTHER)
     */
    default String getCategory() { return "OTHER"; }
    
    /**
     * @return true if command should be placed on undo stack.
     */
    default boolean isUndoable() { return true; }
}
