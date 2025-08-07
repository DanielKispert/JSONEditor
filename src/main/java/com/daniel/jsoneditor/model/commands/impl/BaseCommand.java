package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.Command;

public abstract class BaseCommand implements Command
{
    protected final WritableModelInternal model;
    
    public BaseCommand(WritableModelInternal model)
    {
        this.model = model;
    }
}
