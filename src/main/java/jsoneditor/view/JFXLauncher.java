package jsoneditor.view;

import javafx.application.Application;
import javafx.stage.Stage;
import jsoneditor.controller.impl.ControllerImpl;
import jsoneditor.model.impl.ModelImpl;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.StateMachineImpl;

public class JFXLauncher extends Application
{
    
    
    
    public static void launchJFXApplication(String[] args)
    {
        launch(args);
    }
    
    
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("JSON Editor");
        StateMachine stateMachine = new StateMachineImpl();
        ModelImpl model = new ModelImpl(stateMachine);
        new ControllerImpl(model, model, stage);
        
    }
}
