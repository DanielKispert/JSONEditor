package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.Command;

public abstract class BaseCommand implements Command
{
    protected final ReadableModel readModel;
    
    protected final WritableModelInternal model;
    
    public BaseCommand(ReadableModel readModel, WritableModelInternal model)
    {
        this.readModel = readModel;
        this.model = model;
    }
}
