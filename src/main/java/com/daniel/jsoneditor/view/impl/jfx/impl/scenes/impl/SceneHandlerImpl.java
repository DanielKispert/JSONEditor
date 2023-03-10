package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.SceneHandler;

public abstract class SceneHandlerImpl implements SceneHandler
{
    protected final Controller controller;
    
    protected final ReadableModel model;
    
    public SceneHandlerImpl(Controller controller, ReadableModel model)
    {
        this.controller = controller;
        this.model = model;
    }
}
