package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.net.URL;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;


public class ButtonHelper
{
    public static void setButtonImage(Button button, String path)
    {
        if (button == null || path == null)
        {
            return;
        }
        URL image = ButtonHelper.class.getResource(path);
        if (image == null)
        {
            System.out.println("Image at " + path + " not found");
            return;
        }
        ImageView imageView = new ImageView(image.toExternalForm());
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(20);
        imageView.fitWidthProperty().bind(button.widthProperty());
        button.setGraphic(imageView);
        
    }
}
