package com.daniel.jsoneditor.view.impl.jfx.popups;

import javafx.stage.Popup;
import javafx.stage.Window;

public abstract class BasePopup<T>
{
    protected final Popup popup;
    protected Window owner;
    protected double posX;
    protected double posY;
    
    public BasePopup()
    {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
    }
    
    public void setPopupPosition(Window owner, double x, double y)
    {
        this.owner = owner;
        this.posX = x;
        this.posY = y;
    }
    
    public void show()
    {
        if (!popup.isShowing() && owner != null)
        {
            popup.show(owner, posX, posY);
            moveVertically(popup.getHeight());
        }
    }
    
    public void hide()
    {
        if (popup.isShowing())
        {
            popup.hide();
        }
    }
    
    protected void moveVertically(double newHeight)
    {
        popup.setY(posY - newHeight);
    }
    
    public abstract void setItems(T items);
}