package com.daniel.jsoneditor.controller.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class PropertiesHelper
{
    public static void writePropertiesToFile(Properties properties)
    {
        try (FileOutputStream output = new FileOutputStream("jsoneditor.properties"))
        {
            properties.store(output, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
            System.out.println("jsoneditor.properties not found");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return properties;
    }
}
