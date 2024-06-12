package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import javafx.collections.ListChangeListener;
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
        
        getItems().addListener((ListChangeListener.Change<? extends javafx.scene.Node> c) ->
        {
            double[] oldPositions = getDividerPositions();
            
            while (c.next())
            {
                double[] newPositions = new double[getDividers().size()];
                double adjustmentFactor = c.wasAdded() ? (1 - 1.0 / (newPositions.length + 1)) : (1 + 1.0 / (newPositions.length + 1));
                
                for (int i = 0; i < newPositions.length; i++)
                {
                    newPositions[i] = oldPositions[i] * adjustmentFactor;
                    if (c.wasAdded() && i == newPositions.length - 1)
                    {
                        newPositions[i] = 1 - (1.0 / (newPositions.length + 1));
                    }
                }
                setDividerPositions(newPositions);
            }
        });
    }
}
