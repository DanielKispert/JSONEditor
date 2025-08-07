package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;


public class SetValueAtNodeCommand extends BaseCommand
{
    
    private final String parentPath;
    
    private final String propertyName;
    
    private final Object newValue;
    
    private Object oldValue;
    
    public SetValueAtNodeCommand(WritableModelInternal model, String parentPath, String propertyName,
            Object newValue)
    {
        super(model);
        this.parentPath = parentPath;
        this.propertyName = propertyName;
        this.newValue = newValue;
    }
    
    @Override
    public void execute()
    {
        oldValue = model.setValueAtPath(parentPath, propertyName, newValue);
    }
    
    @Override
    public void undo()
    {
        model.setValueAtPath(parentPath, propertyName, oldValue);
    }
}
