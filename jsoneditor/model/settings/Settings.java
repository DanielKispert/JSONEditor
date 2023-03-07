package jsoneditor.model.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

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
