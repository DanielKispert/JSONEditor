package com.daniel.jsoneditor.view.impl.jfx.impl.scenes;

import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface SceneHandler
{
    Scene getScene(Stage stage);
    
    UIHandler getHandlerForToasting();
}
