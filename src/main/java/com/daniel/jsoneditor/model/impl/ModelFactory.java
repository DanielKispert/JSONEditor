package com.daniel.jsoneditor.model.impl;

import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;

/** Single construction point for {@link ModelImpl} instances. */
public final class ModelFactory
{
    private ModelFactory()
    {
        // utility class
    }

    /**
     * Creates a fresh, empty {@link ModelImpl} ready to be populated via
     * {@code jsonAndSchemaSuccessfullyValidated(...)}.
     */
    public static ModelImpl createEmpty()
    {
        return new ModelImpl(new EventSenderImpl());
    }
}
