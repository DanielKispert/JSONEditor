package com.daniel.jsoneditor.view.impl.jfx.toast;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.validation.ValidationError;
import com.daniel.jsoneditor.view.impl.jfx.PathDisplayConverter;


/**
 * View-layer formatter that turns a structured {@link ValidationError} into a user-readable message.
 * The error-location path is converted via {@link PathDisplayConverter}; the referenced object path stays raw.
 */
public final class ValidationErrorFormatter
{
    private ValidationErrorFormatter()
    {
    }

    /**
     * Formats the given validation error as a human-readable toast message.
     * The error-location path is converted to a display name; referenced object paths remain raw.
     *
     * @param error the structured validation error
     * @param model the model used for path-to-display-name resolution
     * @return a formatted, user-facing error message
     */
    public static String format(final ValidationError error, final ReadableModel model)
    {
        final String displayPath = PathDisplayConverter.convertToDisplay(model, error.getPath());
        switch (error.getType())
        {
            case EMPTY_KEY:
                return String.format("Invalid reference at '%s': Empty or missing reference key", displayPath);
            case DANGLING_REFERENCE:
                return String.format(
                    "Invalid reference at '%s': Cannot find %s with key '%s' in '%s'",
                    displayPath,
                    error.getReferencingKey(),
                    error.getObjectKey(),
                    error.getReferencedObjectPath()
                );
            default:
                return String.format("Invalid reference at '%s'", displayPath);
        }
    }
}
