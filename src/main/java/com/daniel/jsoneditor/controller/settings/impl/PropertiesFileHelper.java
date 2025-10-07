package com.daniel.jsoneditor.controller.settings.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropertiesFileHelper
{
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileHelper.class);
    
    public static void writePropertiesToFile(Properties properties)
    {
        try (FileOutputStream output = new FileOutputStream("jsoneditor.properties"))
        {
            properties.store(output, null);
        }
        catch (IOException e)
        {
            logger.error("IOException while writing properties file", e);
        }
    }
    
    public static Properties readPropertiesFromFile()
    {
        Properties properties = new Properties();
        try (FileInputStream propertiesFile = new FileInputStream("jsoneditor.properties"))
        {
            properties.load(propertiesFile);
        }
        catch (FileNotFoundException e)
        {
            logger.info("jsoneditor.properties not found");
        }
        catch (IOException e)
        {
            logger.error("IOException while reading properties file", e);
        }
        return properties;
    }
}
