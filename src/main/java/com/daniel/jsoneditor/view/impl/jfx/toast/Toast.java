package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.Scene;
import javafx.scene.paint.Color;


public interface Toast
{
    void show(Scene scene, String message, Color color, int duration);
}
