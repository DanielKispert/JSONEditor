package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.AppService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JFXLauncher extends Application
{
    public static void launchJFXApplication(String[] args)
    {
        launch(args);
    }
    
    
    @Override
    public void start(Stage stage)
    {
        // Don't exit JavaFX when last window closes — AppService handles that
        Platform.setImplicitExit(false);
        stage.close();
        
        final AppService appService = new AppService();
        appService.createWindow();
    }
}
