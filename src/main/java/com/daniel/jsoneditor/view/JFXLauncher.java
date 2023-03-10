package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.StateMachine;
import com.daniel.jsoneditor.model.statemachine.impl.StateMachineImpl;
import javafx.application.Application;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.impl.ControllerImpl;

public class JFXLauncher extends Application
{
    
    
    
    public static void launchJFXApplication(String[] args)
    {
        launch(args);
    }
    
    
    @Override
    public void start(Stage stage)
    {
        stage.setTitle("JSON Editor");
        StateMachine stateMachine = new StateMachineImpl();
        ModelImpl model = new ModelImpl(stateMachine);
        new ControllerImpl(model, model, stage);
        
    }
}
