package com.daniel.jsoneditor.view.impl.jfx.buttons;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;


/**
 * A floating transparent button that displays graph interaction help on hover only (non-clickable).
 */
public class GraphInfoButton extends Button
{
    private static final String HELP_TEXT = "- Double-click node: expand graph\n"
            + "- Double-click edge: remove target vertex and orphaned descendants.\n";

    /**
     * Creates a new floating info button with transparent styling and tooltip functionality.
     */
    public GraphInfoButton()
    {
        super();
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_info_white_24dp.png");
        getStyleClass().add("floating-transparent-button");
        
        final Tooltip tooltip = new Tooltip(HELP_TEXT);
        tooltip.setShowDuration(Duration.INDEFINITE);
        setTooltip(tooltip);
        //nothing happens when we click the button - intended
        setFocusTraversable(false);
        addEventFilter(MouseEvent.MOUSE_PRESSED, MouseEvent::consume);
        addEventFilter(MouseEvent.MOUSE_RELEASED, MouseEvent::consume);
        addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
    }
}
