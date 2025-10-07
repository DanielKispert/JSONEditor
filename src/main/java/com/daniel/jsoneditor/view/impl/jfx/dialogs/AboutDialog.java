package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AboutDialog extends ThemedAlert
{
    private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);

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
            logger.error("IOException on trying to load version property", e);
        }
        String versionNumber = versionProperties.getProperty("version");
        setTitle("About");
        setHeaderText("JSON Editor");
        setContentText("Version " + versionNumber);
    }
}
