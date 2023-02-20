package jsoneditor.model.settings;

public class Settings
{
    private final ButtonSetting[] buttons;
    
    
    public Settings(ButtonSetting[] buttons)
    {
        this.buttons = buttons;
    }
    
    public ButtonSetting[] getButtons()
    {
        return buttons;
    }
}
