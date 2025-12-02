package com.daniel.jsoneditor.model.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ValidationResult
{
    private final List<ValidationError> errors;
    
    public ValidationResult(List<ValidationError> errors)
    {
        this.errors = new ArrayList<>(errors);
    }
    
    public boolean isValid()
    {
        return errors.isEmpty();
    }
    
    public List<ValidationError> getErrors()
    {
        return Collections.unmodifiableList(errors);
    }
    
    public int getErrorCount()
    {
        return errors.size();
    }
    
    public String getErrorSummary()
    {
        if (isValid())
        {
            return "No validation errors";
        }
        
        final StringBuilder summary = new StringBuilder();
        summary.append(String.format("Found %d validation error%s:\n",
            errors.size(),
            errors.size() == 1 ? "" : "s"));
        
        for (ValidationError error : errors)
        {
            summary.append("  • ").append(error.getMessage()).append("\n");
        }
        
        return summary.toString();
    }
}

