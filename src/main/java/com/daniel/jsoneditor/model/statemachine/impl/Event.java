package com.daniel.jsoneditor.model.statemachine.impl;

public enum Event
{
    
    LAUNCHING,
    READ_JSON_AND_SCHEMA,
    
    MAIN_EDITOR,
    
    UPDATED_SELECTED_JSON_NODE,
    
    UPDATED_JSON_STRUCTURE,
    
    MOVED_CHILD_OF_SELECTED_JSON_NODE,
    
    REMOVED_SELECTED_JSON_NODE
}
