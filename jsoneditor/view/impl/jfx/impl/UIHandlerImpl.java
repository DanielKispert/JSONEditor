package jsoneditor.view.impl.jfx.impl;

import javafx.application.Application;
import javafx.stage.Stage;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.scenes.JSONSelection;

public class UIHandlerImpl extends Application implements UIHandler
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setScene(new JSONSelection().getScene(stage));
        stage.show();
    }
    
    @Override
    public void startUI()
    {
        launch();
    }
}
