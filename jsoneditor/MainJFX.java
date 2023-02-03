package jsoneditor;

import javafx.application.Application;
import javafx.stage.Stage;
import jsoneditor.controller.impl.ControllerImpl;
import jsoneditor.model.impl.ModelImpl;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.StateMachineImpl;

public class MainJFX extends Application
{
    
    
    
    public static void main(String[] args)
    {
        launch(args);
    }
    
    
    @Override
    public void start(Stage stage) throws Exception
    {
        StateMachine stateMachine = new StateMachineImpl();
        ModelImpl model = new ModelImpl(stateMachine);
        new ControllerImpl(model, model, stage);
        
    }
}
