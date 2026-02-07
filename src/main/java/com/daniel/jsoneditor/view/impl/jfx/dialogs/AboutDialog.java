package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.daniel.jsoneditor.util.VersionUtil;


public class AboutDialog extends ThemedAlert
{
    public AboutDialog()
    {
        super(AlertType.INFORMATION);
        setTitle("About");
        setHeaderText("JSON Editor");
        setContentText("Version " + VersionUtil.getVersion());
    }
}
