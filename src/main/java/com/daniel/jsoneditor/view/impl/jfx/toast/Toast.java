package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.paint.Color;
import javafx.stage.Stage;


public interface Toast
{
    void show(Stage stage, String message, Color color);
}
