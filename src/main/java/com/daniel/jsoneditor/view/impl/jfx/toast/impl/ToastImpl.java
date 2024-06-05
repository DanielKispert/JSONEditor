package com.daniel.jsoneditor.view.impl.jfx.toast.impl;

import com.daniel.jsoneditor.view.impl.jfx.toast.Toast;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class ToastImpl implements Toast
{
    private static final int SLIDE_DURATION = 250;
    private static final int FADE_DURATION = 250;
    
    @Override
    public void show(Stage ownerStage, String message, Color color, int duration)
    {
        Popup toastPopup = new Popup();
        
        HBox root = createToastView(message, color);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        toastPopup.getContent().add(root);
        
        toastPopup.show(ownerStage,
                ownerStage.getX() + (ownerStage.getWidth() - root.getWidth()) / 2,
                ownerStage.getY() + ownerStage.getHeight() - 55);
        
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
                    Thread.sleep(duration * 1000L);
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
                hideTransition.setOnFinished((aeb) -> toastPopup.hide());
                hideTransition.play();
            }).start();
        });
    }
    
    private HBox createToastView(String message, Color color)
    {
        Text text = new Text(message);
        text.setFill(color);
        text.getStyleClass().add("toast");
        
        ImageView icon;
        if (color.equals(Color.GREEN))
        {
            icon = new ImageView(new Image("/icons/material/darkmode/outline_checkmark_24dp.png"));
        }
        else
        {
            icon = new ImageView(new Image("/icons/material/darkmode/outline_close_24dp.png"));
        }
        icon.setFitHeight(15);
        icon.setFitWidth(15);
        
        Label iconArea = new Label("", icon);
        iconArea.setStyle("-fx-background-color: " + toRgbString(color) + ";");
        iconArea.getStyleClass().add("toast-icon-area");
        iconArea.setMaxSize(20, 20);
        
        HBox borderBox = new HBox(iconArea, text);
        borderBox.setStyle("-fx-border-color: " + toRgbString(color) + ";");
        borderBox.getStyleClass().add("toast");
        borderBox.setAlignment(Pos.CENTER);
        borderBox.setSpacing(5);
        
        return borderBox;
    }
    
    private String toRgbString(Color color)
    {
        return String.format("rgba(%d, %d, %d, %f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255), color.getOpacity());
    }
}
