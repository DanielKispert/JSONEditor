package com.daniel.jsoneditor.view.impl.jfx.toast.impl;

import com.daniel.jsoneditor.view.impl.jfx.toast.Toast;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class ToastImpl implements Toast
{
    @Override
    public void show(Scene scene, String message, Color color, int duration)
    {
        // Create a new stage for the toast
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        
        // Create a label for the toast message
        Label toastLabel = new Label(message);
        toastLabel.setWrapText(true);
        
        // Create a stack pane for the label
        StackPane root = new StackPane(toastLabel);
        root.setOpacity(0.5);
        
        // Create a scene for the stage
        Scene toastScene = new Scene(root);
        toastScene.setFill(Color.TRANSPARENT);
        toastStage.setScene(toastScene);
        
        // Position the toast in the bottom middle of the scene
        toastStage.setX(scene.getWindow().getX() + scene.getWidth() / 2 - toastStage.getWidth() / 2);
        toastStage.setY(scene.getWindow().getY() + scene.getHeight() - toastStage.getHeight() - 50);
        
        // Create a moving animation
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5), root);
        tt.setByY(-100);
        tt.play();
        
        // Show the toast and wait for it to close
        toastStage.show();
        
        // Set a pause transition to hide the toast after the given time
        PauseTransition delay = new PauseTransition(Duration.seconds(duration));
        delay.setOnFinished(e -> toastStage.close());
        delay.play();
    }
}
