package com.daniel.jsoneditor.controller.settings.impl;

import java.util.Objects;
import java.util.Properties;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.mcp.JsonEditorMcpServer;


public class SettingsControllerImpl implements SettingsController
{
    private final Properties properties;
    
    private EditorDimensions dimensionsCache; //so we don't have to access the properties file every time
    
    public SettingsControllerImpl()
    {
        this.properties = PropertiesFileHelper.readPropertiesFromFile();
    }
    
    @Override
    public void setFileProperties(boolean rememberPaths, String jsonPath, String schemaPath, String settingsPath)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_REMEMBER_PATHS, rememberPaths ? "true" : "false");
        properties.setProperty(PropertyFileKeys.PROPERTY_LAST_JSON_PATH, jsonPath);
        if (schemaPath != null)
        {
            properties.setProperty(PropertyFileKeys.PROPERTY_LAST_SCHEMA_PATH, schemaPath);
        }
        if (settingsPath != null)
        {
            properties.setProperty(PropertyFileKeys.PROPERTY_LAST_SETTINGS_PATH, settingsPath);
        }
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public void setHideEmptyColumns(boolean automaticallyHideEmptyColumns)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_HIDE_EMPTY_COLUMNS, automaticallyHideEmptyColumns ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public void setClusterShape(String symbol)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_CLUSTER_SHAPE, symbol);
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public void setEditorDimensions(int width, int height, boolean startMaximized)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_EDITOR_WIDTH, String.valueOf(width));
        properties.setProperty(PropertyFileKeys.PROPERTY_EDITOR_HEIGHT, String.valueOf(height));
        properties.setProperty(PropertyFileKeys.PROPERTY_START_MAXIMIZED, startMaximized ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
        dimensionsCache = new EditorDimensions(width, height, startMaximized);
    }
    
    @Override
    public EditorDimensions getEditorDimensions()
    {
        if (dimensionsCache == null)
        {
            dimensionsCache = readEditorDimensionsFromProperties();
        }
        return dimensionsCache;
    }
    
    private EditorDimensions readEditorDimensionsFromProperties()
    {
        String widthProperty = properties.getProperty(PropertyFileKeys.PROPERTY_EDITOR_WIDTH);
        String heightProperty = properties.getProperty(PropertyFileKeys.PROPERTY_EDITOR_HEIGHT);
        String maximizedProperty = properties.getProperty(PropertyFileKeys.PROPERTY_START_MAXIMIZED);
        int width = widthProperty == null ? 1400 : Integer.parseInt(widthProperty);
        int height = heightProperty == null ? 800 : Integer.parseInt(heightProperty);
        boolean maximized = "true".equalsIgnoreCase(maximizedProperty);
        return new EditorDimensions(width, height, maximized);
    }
    
    @Override
    public String getClusterShape()
    {
        String shape = properties.getProperty(PropertyFileKeys.PROPERTY_CLUSTER_SHAPE);
        return Objects.requireNonNullElse(shape, "hexagon");
    }
    
    @Override
    public boolean hideEmptyColumns()
    {
        // default is true
        String property = properties.getProperty(PropertyFileKeys.PROPERTY_HIDE_EMPTY_COLUMNS);
        if (property == null)
        {
            return true;
        }
        else
        {
            return "true".equalsIgnoreCase(property);
        }
    }
    
    @Override
    public void setRenameReferencesWhenRenamingObject(boolean renameReferences)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_RENAME_REFERENCES_WHEN_RENAMING_OBJECT, renameReferences ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public boolean renameReferencesWhenRenamingObject()
    {
        // default is true
        String property = properties.getProperty(PropertyFileKeys.PROPERTY_RENAME_REFERENCES_WHEN_RENAMING_OBJECT);
        if (property == null)
        {
            return true;
        }
        else
        {
            return "true".equalsIgnoreCase(property);
        }
    }
    
    @Override
    public String getLastJsonPath()
    {
        return properties.getProperty(PropertyFileKeys.PROPERTY_LAST_JSON_PATH);
    }
    
    @Override
    public String getLastSchemaPath()
    {
        return properties.getProperty(PropertyFileKeys.PROPERTY_LAST_SCHEMA_PATH);
    }
    
    @Override
    public String getLastSettingsPath()
    {
        return properties.getProperty(PropertyFileKeys.PROPERTY_LAST_SETTINGS_PATH);
    }
    
    @Override
    public boolean rememberPaths()
    {
        // default is false
        return "true".equalsIgnoreCase(properties.getProperty(PropertyFileKeys.PROPERTY_REMEMBER_PATHS));
    }
    
    @Override
    public void setDebugMode(boolean debugMode)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_DEBUG_MODE, debugMode ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public boolean isDebugMode()
    {
        // default is false
        return "true".equalsIgnoreCase(properties.getProperty(PropertyFileKeys.PROPERTY_DEBUG_MODE));
    }
    
    @Override
    public void setLogGraphRequests(boolean logGraphRequests)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_LOG_GRAPH_REQUESTS, logGraphRequests ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public boolean isLogGraphRequests()
    {
        return "true".equalsIgnoreCase(properties.getProperty(PropertyFileKeys.PROPERTY_LOG_GRAPH_REQUESTS));
    }
    
    @Override
    public void setMcpServerEnabled(boolean enabled)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_MCP_SERVER_ENABLED, enabled ? "true" : "false");
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public boolean isMcpServerEnabled()
    {
        return "true".equalsIgnoreCase(properties.getProperty(PropertyFileKeys.PROPERTY_MCP_SERVER_ENABLED));
    }
    
    @Override
    public void setMcpServerPort(int port)
    {
        properties.setProperty(PropertyFileKeys.PROPERTY_MCP_SERVER_PORT, String.valueOf(port));
        PropertiesFileHelper.writePropertiesToFile(properties);
    }
    
    @Override
    public int getMcpServerPort()
    {
        final String portStr = properties.getProperty(PropertyFileKeys.PROPERTY_MCP_SERVER_PORT);
        if (portStr == null)
        {
            return JsonEditorMcpServer.DEFAULT_PORT;
        }
        try
        {
            return Integer.parseInt(portStr);
        }
        catch (NumberFormatException e)
        {
            return JsonEditorMcpServer.DEFAULT_PORT;
        }
    }
}
