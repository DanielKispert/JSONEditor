package jsoneditor.view.impl.jfx.impl.scenes.impl;

import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.view.impl.jfx.impl.scenes.SceneHandler;

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
