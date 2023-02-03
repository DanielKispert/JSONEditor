package jsoneditor.view.impl.jfx.impl;

import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelection;

public class UIHandlerImpl implements UIHandler
{
    private final Controller controller;
    
    private final Stage stage;
    
    public UIHandlerImpl(Controller controller, Stage stage)
    {
        this.controller = controller;
        this.stage = stage;
    }
    
    @Override
    public void showSelectJsonAndSchema()
    {
        stage.setScene(new JSONSelection(controller).getScene(stage));
        stage.show();
    }
}
