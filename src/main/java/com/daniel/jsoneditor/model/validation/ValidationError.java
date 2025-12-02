package com.daniel.jsoneditor.model.validation;


public class ValidationError
{
    private final String path;
    
    private final String message;
    
    public ValidationError(String path, String message)
    {
        this.path = path;
        this.message = message;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    @Override
    public String toString()
    {
        return message;
    }
}

