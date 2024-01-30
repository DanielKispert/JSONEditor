package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javafx.scene.control.Alert;


public class AboutDialog extends ThemedAlert
{
    public AboutDialog()
    {
        super(AlertType.INFORMATION);
        Properties versionProperties = new Properties();
        InputStream versionFile = getClass().getClassLoader().getResourceAsStream("version.properties");
        try
        {
            versionProperties.load(versionFile);
        }
        catch (IOException e)
        {
            System.out.println("IOException on trying to load version property");
        }
        String versionNumber = versionProperties.getProperty("version");
        setTitle("About");
        setHeaderText("JSON Editor");
        setContentText("Version " + versionNumber);
    }
}
