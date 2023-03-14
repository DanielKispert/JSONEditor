package com.daniel.jsoneditor.model.settings;

public class Settings
{
    private ButtonSetting[] buttons;
    
    public Settings()
    {
    }
    
    public Settings(ButtonSetting[] buttons)
    {
        if (buttons != null)
        {
            this.buttons = buttons;
        }
        else
        {
            this.buttons = new ButtonSetting[0];
        }
    }
    
    public ButtonSetting[] getButtons()
    {
        return buttons;
    }
}
