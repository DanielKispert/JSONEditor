package com.daniel.jsoneditor.model.statemachine.impl;

/**
 * events are sent from model (or controller) and update the UI
 */
public enum EventEnum
{
    
    LAUNCHING,
    
    READ_JSON_AND_SCHEMA,
    
    MAIN_EDITOR,
    
    RELOADED_JSON_FROM_DISK,
    
    CREATED_NODE_FOR_LINKING,
    
    RESET_SUCCESSFUL,
    
    MOVED_CHILD_OF_SELECTED_JSON_NODE,
    
    REMOVED_SELECTED_JSON_NODE,
    
    SAVING_SUCCESSFUL,
    
    EXPORT_SUCCESSFUL,
    
    EXPORT_FAILED_DEPENDENCY_LOOP,
    
    IMPORT_SUCCESSFUL,
    
    SAVING_FAILED,
    
    COMMAND_APPLIED // new command (execute/undo/redo) with metadata
}
