package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.SceneHandler;

public abstract class SceneHandlerImpl implements SceneHandler
{
    private final UIHandler handler;
    
    protected final Controller controller;
    
    protected final ReadableModel model;
    
    public SceneHandlerImpl(UIHandler handler, Controller controller, ReadableModel model)
    {
        this.handler = handler;
        this.controller = controller;
        this.model = model;
    }
    
    @Override
    public final UIHandler getHandlerForToasting()
    {
        return handler;
    }
}
