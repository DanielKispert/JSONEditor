package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class AutoAdjustingSplitPane extends SplitPane
{
    /** Prevents multiple Platform.runLater calls from being queued when items change in quick succession. */
    private boolean recalculationPending = false;

    public AutoAdjustingSplitPane()
    {
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("auto-adjusting-split-pane");
        getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());

        getItems().addListener((ListChangeListener.Change<? extends Node> c) ->
        {
            while (c.next())
            {
                if (!recalculationPending)
                {
                    recalculationPending = true;
                    // Defer to the next JavaFX pulse so all items are added before recalculating,
                    // and the layout pass can provide more accurate prefHeight values.
                    Platform.runLater(() ->
                    {
                        recalculationPending = false;
                        recalculateDividerPositions();
                    });
                }
            }
        });
        
        // After the very first layout the actual available size is known.
        // Re-run the calculation so the absolute pixel allocation can be used
        // instead of the proportional fallback.
        heightProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal)
            {
                if (newVal.doubleValue() > 0)
                {
                    heightProperty().removeListener(this);
                    recalculateDividerPositions();
                }
            }
        });
    }

    /**
     * Recomputes all divider positions by delegating to
     * {@link DividerPositionCalculator#calculateFromPreferredAndMinimumSizes}.
     */
    private void recalculateDividerPositions()
    {
        final int n = getItems().size();
        if (n < 2)
        {
            return;
        }
        final boolean vertical = getOrientation() == Orientation.VERTICAL;
        final double[] prefSizes = new double[n];
        final double[] minSizes = new double[n];
        for (int i = 0; i < n; i++)
        {
            final Node item = getItems().get(i);
            prefSizes[i] = Math.max(vertical ? item.prefHeight(-1) : item.prefWidth(-1), 0);
            minSizes[i] = Math.max(vertical ? item.minHeight(-1) : item.minWidth(-1), 0);
        }
        final double available = vertical ? getHeight() : getWidth();
        final double[] positions =
                DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(prefSizes, minSizes,
                        available);
        setDividerPositions(positions);
    }

    @Override
    protected double computePrefWidth(double height)
    {
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            double totalPrefWidth = 0;
            for (Node item : getItems())
            {
                totalPrefWidth += item.prefWidth(height);
            }
            return totalPrefWidth;
        }
        else
        {
            double maxPrefWidth = 0;
            for (Node item : getItems())
            {
                maxPrefWidth = Math.max(maxPrefWidth, item.prefWidth(height));
            }
            return maxPrefWidth;
        }
    }

    @Override
    protected double computePrefHeight(double width)
    {
        if (getOrientation() == Orientation.VERTICAL)
        {
            double totalPrefHeight = 0;
            for (Node item : getItems())
            {
                totalPrefHeight += item.prefHeight(width);
            }
            return totalPrefHeight;
        }
        else
        {
            double maxPrefHeight = 0;
            for (Node item : getItems())
            {
                maxPrefHeight = Math.max(maxPrefHeight, item.prefHeight(width));
            }
            return maxPrefHeight;
        }
    }
}
