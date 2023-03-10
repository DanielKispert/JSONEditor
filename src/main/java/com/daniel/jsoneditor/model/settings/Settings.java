package com.daniel.jsoneditor.model.settings;

public class Settings
{
    private ButtonSetting[] buttons;
    
    public Settings()
    {
    }
    public Settings(ButtonSetting[] buttons)
    {
        this.buttons = buttons;
    }
    
    public ButtonSetting[] getButtons()
    {
        return buttons;
    }
}
