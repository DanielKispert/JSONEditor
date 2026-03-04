package com.daniel.jsoneditor.view.impl.jfx.buttons;

import java.util.function.BooleanSupplier;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Button that toggles the sidebar (navbar) visibility in the editor.
 */
public class ToggleSidebarButton extends Button
{
    private static final String ICON_COLLAPSE = "/icons/material/darkmode/outline_menu_close_white_24dp.png";
    private static final String ICON_EXPAND = "/icons/material/darkmode/outline_menu_open_white_24dp.png";
    
    private final BooleanSupplier collapsedSupplier;
    
    public ToggleSidebarButton(Runnable toggleAction, BooleanSupplier collapsedSupplier)
    {
        super();
        this.collapsedSupplier = collapsedSupplier;
        updateAppearance();
        setOnAction(event -> toggleAction.run());
    }
    
    public void updateAppearance()
    {
        if (collapsedSupplier.getAsBoolean())
        {
            ButtonHelper.setButtonImage(this, ICON_EXPAND);
            setTooltip(new Tooltip("Show Sidebar"));
        }
        else
        {
            ButtonHelper.setButtonImage(this, ICON_COLLAPSE);
            setTooltip(new Tooltip("Hide Sidebar"));
        }
    }
}




