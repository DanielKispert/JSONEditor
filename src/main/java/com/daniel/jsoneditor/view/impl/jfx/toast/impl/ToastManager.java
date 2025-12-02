package com.daniel.jsoneditor.view.impl.jfx.toast.impl;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;

public class ToastManager
{
    private final Queue<PendingToast> pendingToasts = new LinkedList<>();
    
    private final Object lock = new Object();
    
    private boolean isDisplayingToast = false;
    
    /**
     * Shows a toast notification. If a toast is currently displayed, queues it for later display.
     *
     * @param ownerStage The stage to show the toast on
     * @param message The message to display
     * @param color The color of the toast
     * @param duration Duration in seconds
     */
    public void showToast(Stage ownerStage, String message, Color color, int duration)
    {
        synchronized (lock)
        {
            pendingToasts.offer(new PendingToast(ownerStage, message, color, duration));
            processNextToast();
        }
    }
    
    private void processNextToast()
    {
        synchronized (lock)
        {
            if (isDisplayingToast || pendingToasts.isEmpty())
            {
                return;
            }
            
            isDisplayingToast = true;
            final PendingToast pending = pendingToasts.poll();
            
            if (pending != null)
            {
                displayToast(pending);
            }
        }
    }
    
    private void displayToast(PendingToast pending)
    {
        Platform.runLater(() -> {
            new ToastImpl().show(pending.ownerStage, pending.message, pending.color, pending.duration,
                    this::onToastFinished);
        });
    }
    
    private void onToastFinished()
    {
        synchronized (lock)
        {
            isDisplayingToast = false;
            processNextToast();
        }
    }
    
    private static class PendingToast
    {
        final Stage ownerStage;
        final String message;
        final Color color;
        final int duration;
        
        PendingToast(Stage ownerStage, String message, Color color, int duration)
        {
            this.ownerStage = ownerStage;
            this.message = message;
            this.color = color;
            this.duration = duration;
        }
    }
}

