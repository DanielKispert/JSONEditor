package com.daniel.jsoneditor.model.changes;

/**
 * Type of change that happened to the model.
 */
public enum ChangeType {
    ADD,
    REMOVE,
    REPLACE,
    MOVE,
    SORT // array order changed as single atomic operation
}
