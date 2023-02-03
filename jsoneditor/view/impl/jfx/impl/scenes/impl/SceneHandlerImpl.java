package jsoneditor.view.impl.jfx.impl.scenes.impl;

import jsoneditor.controller.Controller;
import jsoneditor.view.impl.jfx.impl.scenes.SceneHandler;

public abstract class SceneHandlerImpl implements SceneHandler
{
    protected final Controller controller;
    
    public SceneHandlerImpl(Controller controller)
    {
        this.controller = controller;
    }
}
