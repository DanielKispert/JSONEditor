package com.daniel.jsoneditor.model.validation;

import java.util.Collections;
import java.util.List;


/**
 * Thrown by model mutation methods when a proposed change would violate schema constraints.
 * Carries the path and detailed validation error messages from the schema validator.
 */
public class ModelValidationException extends RuntimeException
{
    private final String path;
    
    private final List<String> validationErrors;
    
    public ModelValidationException(String path, List<String> validationErrors)
    {
        super(formatMessage(path, validationErrors));
        this.path = path;
        this.validationErrors = Collections.unmodifiableList(validationErrors);
    }
    
    public String getPath()
    {
        return path;
    }
    
    public List<String> getValidationErrors()
    {
        return validationErrors;
    }
    
    private static String formatMessage(String path, List<String> errors)
    {
        if (errors.isEmpty())
        {
            return "Validation failed at " + path;
        }
        return "Validation failed at " + path + ": " + errors.get(0)
            + (errors.size() > 1 ? " (+" + (errors.size() - 1) + " more)" : "");
    }
}
