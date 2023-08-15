package com.daniel.jsoneditor.model.settings;

public class Settings
{
    private ButtonSetting[] buttons;
    
    private IdentifierSetting[] identifiers;
    
    public Settings()
    {
    }
    
    public Settings(ButtonSetting[] buttons, IdentifierSetting[] identifiers)
    {
        if (buttons != null)
        {
            this.buttons = buttons;
        }
        else
        {
            this.buttons = new ButtonSetting[0];
        }
        if (identifiers != null)
        {
            this.identifiers = identifiers;
        }
        else
        {
            this.identifiers = new IdentifierSetting[0];
        }
    }
    
    public ButtonSetting[] getButtons()
    {
        return buttons;
    }
    
    public IdentifierSetting[] getIdentifiers()
    {
        if (identifiers == null)
        {
            return new IdentifierSetting[0];
        }
        else
        {
            return identifiers;
        }
    }
}
