package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.net.URL;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ButtonHelper
{
    private static final Logger logger = LoggerFactory.getLogger(ButtonHelper.class);

    public static void setButtonImage(Button button, String path)
    {
        if (button == null || path == null)
        {
            return;
        }
        URL image = ButtonHelper.class.getResource(path);
        if (image == null)
        {
            logger.warn("Image at {} not found", path);
            return;
        }
        ImageView imageView = new ImageView(image.toExternalForm());
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(20);
        imageView.fitWidthProperty().bind(button.widthProperty());
        button.setGraphic(imageView);
        
    }
}
