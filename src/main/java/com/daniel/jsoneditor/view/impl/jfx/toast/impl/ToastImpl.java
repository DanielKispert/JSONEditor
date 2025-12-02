package com.daniel.jsoneditor.view.impl.jfx.toast.impl;

import com.daniel.jsoneditor.view.impl.jfx.toast.Toast;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ToastImpl implements Toast
{
    private static final int SLIDE_DURATION = 250;
    
    private static final int FADE_DURATION = 250;
    
    private static final int MIN_DURATION_MS = 1500;
    
    private static final int MAX_DURATION_MS = 5000;
    
    private static final int MS_PER_CHARACTER = 100;
    
    @Override
    public void show(Stage ownerStage, String message, Color color)
    {
        show(ownerStage, message, color, null);
    }
    
    public void show(Stage ownerStage, String message, Color color, Runnable onFinished)
    {
        final int actualDuration = calculateDuration(message);
        
        final Popup toastPopup = new Popup();
        
        final HBox root = createToastView(message, color);
        final Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        toastPopup.getContent().add(root);
        
        // Show popup initially off-screen to calculate dimensions
        toastPopup.show(ownerStage, -1000, -1000);
        
        // Use Platform.runLater to center horizontally after layout calculation
        Platform.runLater(() -> {
            final double centerX = ownerStage.getX() + (ownerStage.getWidth() - root.getBoundsInLocal().getWidth()) / 2;
            final double bottomY = ownerStage.getY() + ownerStage.getHeight() - 55;
            
            toastPopup.setX(centerX);
            toastPopup.setY(bottomY);
        });
        
        root.setTranslateY(10);
        root.setOpacity(0);
        
        Timeline moveUpTimeline = new Timeline();
        KeyFrame moveUpKey = new KeyFrame(Duration.millis(SLIDE_DURATION), new KeyValue(root.translateYProperty(), 0));
        moveUpTimeline.getKeyFrames().add(moveUpKey);
        
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey = new KeyFrame(Duration.millis(FADE_DURATION), new KeyValue(root.opacityProperty(), 0.8));
        fadeInTimeline.getKeyFrames().add(fadeInKey);
        
        ParallelTransition showTransition = new ParallelTransition(moveUpTimeline, fadeInTimeline);
        showTransition.play();
        
        showTransition.setOnFinished((ae) -> {
            new Thread(() -> {
                try
                {
                    Thread.sleep(actualDuration);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                Timeline moveDownTimeline = new Timeline();
                KeyFrame moveDownKey = new KeyFrame(Duration.millis(SLIDE_DURATION), new KeyValue(root.translateYProperty(), 10));
                moveDownTimeline.getKeyFrames().add(moveDownKey);
                
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey = new KeyFrame(Duration.millis(FADE_DURATION), new KeyValue(root.opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey);
                
                ParallelTransition hideTransition = new ParallelTransition(moveDownTimeline, fadeOutTimeline);
                hideTransition.setOnFinished((aeb) -> {
                    toastPopup.hide();
                    if (onFinished != null)
                    {
                        onFinished.run();
                    }
                });
                hideTransition.play();
            }).start();
        });
    }
    
    private HBox createToastView(String message, Color color)
    {
        final Label textLabel = new Label(message);
        textLabel.setTextFill(color);
        textLabel.getStyleClass().add("toast");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(600);
        
        ImageView icon;
        if (color.equals(Color.GREEN))
        {
            icon = new ImageView(new Image("/icons/material/darkmode/outline_checkmark_24dp.png"));
        }
        else if (color.equals(Color.ORANGE))
        {
            icon = new ImageView(new Image("/icons/material/darkmode/outline_pageview_white_24dp.png"));
        }
        else
        {
            icon = new ImageView(new Image("/icons/material/darkmode/outline_close_white_24dp.png"));
        }
        icon.setFitHeight(15);
        icon.setFitWidth(15);
        
        final Label iconArea = new Label("", icon);
        iconArea.setStyle("-fx-background-color: " + toRgbString(color) + ";");
        iconArea.getStyleClass().add("toast-icon-area");
        iconArea.setMaxSize(20, 20);
        iconArea.setMinSize(20, 20);
        
        final HBox borderBox = new HBox(iconArea, textLabel);
        borderBox.setStyle("-fx-border-color: " + toRgbString(color) + ";");
        borderBox.getStyleClass().add("toast");
        borderBox.setAlignment(Pos.CENTER_LEFT);
        borderBox.setSpacing(5);
        borderBox.setMaxWidth(650);
        
        return borderBox;
    }
    
    private String toRgbString(Color color)
    {
        return String.format("rgba(%d, %d, %d, %f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255), color.getOpacity());
    }
    
    /**
     * Calculates the display duration based on message length.
     * Minimum 1.5 seconds, maximum 5 seconds, then 100ms per character (reading speed ~300 words per minute).
     *
     * @param message The message to display
     * @return Duration in milliseconds
     */
    private int calculateDuration(String message)
    {
        final int calculatedDuration = message.length() * MS_PER_CHARACTER;
        return Math.min(Math.max(MIN_DURATION_MS, calculatedDuration), MAX_DURATION_MS);
    }
}
