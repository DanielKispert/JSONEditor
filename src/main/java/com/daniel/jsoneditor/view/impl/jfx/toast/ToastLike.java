package com.daniel.jsoneditor.view.impl.jfx.toast;

import javafx.scene.paint.Color;


/**
 * Common interface for both enum constants and dynamically created toasts.
 */
public interface ToastLike
{
    String getMessage();
    Color getColor();
}
