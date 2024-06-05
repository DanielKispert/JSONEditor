package com.daniel.jsoneditor.view.impl.jfx.toast.impl;

import com.daniel.jsoneditor.view.impl.jfx.toast.Toast;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class ToastImpl implements Toast
{
    private static final int SLIDE_DURATION = 250;
    
    /**
     * display toast message with slide-in and slide-out
     *
     * @param ownerStage
     *         The stage that owns this toast.
     * @param message
     *         The message to be displayed in the toast.
     * @param color
     *         The color of the toast.
     * @param duration
     *         The duration for which the toast is displayed.
     */
    @Override
    public void show(Stage ownerStage, String message, Color color, int duration)
    {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        
        StackPane root = createToastView(message, color);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        toastStage.setScene(scene);
        toastStage.show();
        
        toastStage.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - root.getWidth() / 2);
        toastStage.setY(ownerStage.getY() + ownerStage.getHeight() - root.getHeight() - 10);
        
        root.setTranslateY(root.getHeight());
        
        Timeline moveUpTimeline = new Timeline();
        KeyFrame moveUpKey = new KeyFrame(Duration.millis(SLIDE_DURATION), new KeyValue(root.translateYProperty(), 0));
        moveUpTimeline.getKeyFrames().add(moveUpKey);
        moveUpTimeline.play();
        
        moveUpTimeline.setOnFinished((ae) -> {
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
                KeyFrame moveDownKey = new KeyFrame(Duration.millis(SLIDE_DURATION), new KeyValue(root.translateYProperty(), root.getHeight()));
                moveDownTimeline.getKeyFrames().add(moveDownKey);
                moveDownTimeline.setOnFinished((aeb) -> toastStage.close());
                moveDownTimeline.play();
            }).start();
        });
    }
    
    private StackPane createToastView(String message, Color color)
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
        
        HBox hbox = new HBox(iconArea, text);
        hbox.setStyle("-fx-border-color: " + toRgbString(color) + ";");
        hbox.getStyleClass().add("toast");
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5);
        
        return new StackPane(hbox);
    }
    
    private String toRgbString(Color color)
    {
        return String.format("rgba(%d, %d, %d, %f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255), color.getOpacity());
    }
}
