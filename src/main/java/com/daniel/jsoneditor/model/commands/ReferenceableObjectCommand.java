package com.daniel.jsoneditor.model.commands;

/**
 * Marker interface for commands that create referenceable objects.
 * Used by the UI layer to detect when special handling is needed.
 */
public interface ReferenceableObjectCommand extends Command
{
    /**
     * @return the path of the newly created referenceable object after execution
     */
    String getCreatedObjectPath();
}
