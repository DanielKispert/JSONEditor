package com.daniel.jsoneditor.controller;

import com.daniel.jsoneditor.controller.impl.ControllerImpl;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import javafx.stage.Stage;


/**
 * Encapsulates a single app window with its own Model, Controller, View, and Stage.
 * Created by AppService, one per open file.
 */
public class AppWindow
{
    private final Controller controller;
    
    private final Stage stage;
    
    public AppWindow(final AppService appService)
    {
        this.stage = new Stage();
        stage.setTitle("JSON Editor");
        final ModelImpl model = new ModelImpl(new EventSenderImpl());
        this.controller = new ControllerImpl(model, model, stage, appService);
    }
    
    /**
     * Sets up close behavior: shuts down this window's controller.
     *
     * @param onClose callback to run after this window closes (e.g. app exit check)
     */
    public void setOnClose(final Runnable onClose)
    {
        stage.setOnHiding(event ->
        {
            controller.shutdown();
            if (onClose != null)
            {
                onClose.run();
            }
        });
    }
    
    /** Returns this window's controller. */
    public Controller getController()
    {
        return controller;
    }
    
    /** Returns this window's stage. */
    public Stage getStage()
    {
        return stage;
    }
}
