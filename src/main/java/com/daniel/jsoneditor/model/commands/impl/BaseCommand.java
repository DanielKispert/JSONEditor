package com.daniel.jsoneditor.model.commands.impl;

import com.daniel.jsoneditor.model.WritableModelInternal;
import com.daniel.jsoneditor.model.commands.Command;
import com.daniel.jsoneditor.model.changes.ModelChange;
import java.util.Collections;
import java.util.List;

<<<<<<< Updated upstream
public abstract class BaseCommand implements Command
{
    protected final WritableModelInternal model;
    
    public BaseCommand(WritableModelInternal model)
    {
=======
public abstract class BaseCommand implements Command {
    protected final ReadableModel readModel;
    protected final WritableModelInternal model;

    public BaseCommand(ReadableModel readModel, WritableModelInternal model) {
        this.readModel = readModel;
>>>>>>> Stashed changes
        this.model = model;
    }

    // convenience if a subclass has no changes (not used now)
    protected List<ModelChange> noChanges() { return Collections.emptyList(); }
}
