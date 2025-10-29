package com.daniel.jsoneditor.model.commands;

public enum CommandCategory
{
    /** Commands that modify the structure of the JSON document (add, remove, move nodes) */
    STRUCTURE,
    
    /** Commands that modify values of existing nodes */
    VALUE,
    
    /** Commands that operate on individual nodes */
    NODE,
    
    /** Commands that don't fit into other categories */
    OTHER
}
