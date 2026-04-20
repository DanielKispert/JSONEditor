package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;


/**
 * Reusable flash animation that briefly toggles a CSS class on a node.
 * Used by {@link JsonEditorEditorWindow} and its test double.
 */
public final class FlashHelper
{
    private FlashHelper()
    {
        // utility class
    }

    /**
     * Plays a brief flash animation on the given node by toggling the specified CSS class.
     * Stops any previously running flash timeline to prevent CSS class accumulation.
     *
     * @param node             the node to flash
     * @param existingTimeline the previously running timeline (may be {@code null})
     * @param cssClass         the CSS class to toggle
     * @return the new timeline (caller should store this for future calls)
     */
    public static Timeline flash(Node node, Timeline existingTimeline, String cssClass)
    {
        node.requestFocus();
        if (existingTimeline != null)
        {
            existingTimeline.stop();
        }
        node.getStyleClass().removeAll(cssClass);
        final Timeline timeline = new Timeline();
        for (int i = 0; i < 3; i++)
        {
            final boolean on = (i % 2 == 0);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 200), e -> {
                if (on)
                {
                    node.getStyleClass().add(cssClass);
                }
                else
                {
                    node.getStyleClass().remove(cssClass);
                }
            }));
        }
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(3 * 200), e -> node.getStyleClass().remove(cssClass)));
        timeline.play();
        return timeline;
    }
}
