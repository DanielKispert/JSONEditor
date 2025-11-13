package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.view.impl.jfx.popups.HistoryPopup;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * Button to show command history as a floating popup (browser-style navigation)
 */
public class HistoryButton extends Button
{
    private final Controller controller;
    private final Stage parentStage;
    private HistoryPopup historyPopup;
    
    public HistoryButton(Controller controller, Stage parentStage)
    {
        super();
        this.controller = controller;
        this.parentStage = parentStage;
        
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_history_white_24dp.png");
        setOnAction(actionEvent -> showHistoryPopup());
        setTooltip(new Tooltip("Show command history"));
    }
    
    private void showHistoryPopup()
    {
        if (historyPopup != null && historyPopup.isShowing())
        {
            historyPopup.hide();
            return;
        }
        
        historyPopup = new HistoryPopup(controller);
        
        // Position popup below the button (BasePopup will auto-adjust)
        double buttonX = localToScreen(0, 0).getX();
        double buttonY = localToScreen(0, 0).getY() + getHeight();
        
        historyPopup.setPopupPosition(parentStage, buttonX, buttonY);
        historyPopup.show();
    }
}


