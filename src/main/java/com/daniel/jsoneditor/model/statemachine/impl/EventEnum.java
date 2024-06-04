package com.daniel.jsoneditor.model.statemachine.impl;

public enum EventEnum
{
    
    LAUNCHING,
    READ_JSON_AND_SCHEMA,
    
    MAIN_EDITOR,
    
    ADDED_ITEM_TO_ARRAY,
    
    UPDATED_JSON_STRUCTURE,
    
    MOVED_CHILD_OF_SELECTED_JSON_NODE,
    
    REMOVED_SELECTED_JSON_NODE,
    
    SAVING_SUCCESSFUL,
    
    SAVING_FAILED
}
