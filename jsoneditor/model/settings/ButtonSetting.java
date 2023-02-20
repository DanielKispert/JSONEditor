package jsoneditor.model.settings;

public class ButtonSetting
{
    
    private final String title;
    
    private final String target;
    
    
    // intended to be private, will be initialized by reading from json
    private ButtonSetting(String title, String target)
    {
        this.title = title;
        this.target = target;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public String getTarget()
    {
        return target;
    }
}
