package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AutoAdjustingSplitPane extends SplitPane
{
    public AutoAdjustingSplitPane()
    {
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("auto-adjusting-split-pane");
        getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        
        getItems().addListener((ListChangeListener.Change<? extends javafx.scene.Node> c) -> {
            while (c.next())
            {
                int numberOfItems = getItems().size();
                double[] oldPositions = getDividerPositions();
                double[] newPositions = new double[getDividers().size()];
                double adjustmentFactor = c.wasAdded() ? (1 - 1.0 / (newPositions.length + 1)) : (1 + 1.0 / (newPositions.length + 1));
                
                for (int i = 0; i < newPositions.length; i++)
                {
                    // the last added item gets special treatment. We add a new divider to the end, so it is possible at that point to move it
                    // all other dividers should stay at the same relative position
                    // we calculate the space available for the last item and the item that is being added and divide it based on their preferred dimensions
                    if (c.wasAdded() && i == newPositions.length - 1)
                    {
                        double positionOfDividerBeforeLastItem = i != 0 ? newPositions[i - 1] : 0;
                        double preferredSpaceForLastItem = getPreferredDimension(getItems().get(numberOfItems - 2));
                        double preferredSpaceForNewItem = getPreferredDimension(getItems().get(numberOfItems - 1));
                        preferredSpaceForNewItem = preferredSpaceForNewItem == 0 ? preferredSpaceForLastItem : preferredSpaceForNewItem;
                        //fallback, we use half the space if the new item doesn't have a preferred dimension yet
                        double positionOfNewDivider = positionOfDividerBeforeLastItem
                                + (preferredSpaceForLastItem / (preferredSpaceForLastItem + preferredSpaceForNewItem)) * (1
                                - positionOfDividerBeforeLastItem);
                        newPositions[i] = positionOfNewDivider;
                    }
                    else
                    {
                        
                        newPositions[i] = oldPositions[i] * adjustmentFactor;
                    }
                }
                setDividerPositions(newPositions);
            }
        });
    }
    
    private double getPreferredDimension(Node item)
    {
        return getOrientation() == Orientation.VERTICAL ? item.prefHeight(-1) : item.prefWidth(-1);
    }
}
