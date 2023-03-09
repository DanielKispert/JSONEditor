package jsoneditor.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.WritableModel;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.observe.Observer;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.settings.Settings;
import jsoneditor.model.statemachine.impl.Event;
import jsoneditor.view.View;
import jsoneditor.view.impl.ViewImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ControllerImpl implements Controller, Observer
{
    private final static String PROPERTY_LAST_JSON_PATH = "last_json_path";
    private final static String PROPERTY_LAST_SCHEMA_PATH = "last_schema_path";
    private final static String PROPERTY_LAST_SETTINGS_PATH = "last_settings_path";
    
    private final static String PROPERTY_REMEMBER_PATHS = "remember_paths";
    
    private final Properties properties;
    private final WritableModel model;
    
    private final ReadableModel readableModel;
    
    private final View view;
    
    private List<Subject> subjects;
    
    public ControllerImpl(WritableModel model, ReadableModel readableModel, Stage stage)
    {
        properties = new Properties();
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
        this.model = model;
        this.readableModel = readableModel;
        this.subjects = new ArrayList<>();
        this.view = new ViewImpl(readableModel, this, stage);
        this.view.observe(this.readableModel.getForObservation());
    }
    
    
    @Override
    public void update()
    {
    }
    
    @Override
    public void observe(Subject subjectToObserve)
    {
        subjects.add(subjectToObserve);
        subjectToObserve.registerObserver(this);
        
    }
    
    @Override
    public void launchFinished()
    {
        model.sendEvent(Event.READ_JSON_AND_SCHEMA);
    }
    
    @Override
    public void setFileProperties(boolean rememberPaths, String jsonPath, String schemaPath, String settingsPath)
    {
        properties.setProperty(PROPERTY_REMEMBER_PATHS, rememberPaths ? "true" : "false");
        properties.setProperty(PROPERTY_LAST_JSON_PATH, jsonPath);
        properties.setProperty(PROPERTY_LAST_SCHEMA_PATH, schemaPath);
        properties.setProperty(PROPERTY_LAST_SETTINGS_PATH, settingsPath);
        try (FileOutputStream output = new FileOutputStream("jsoneditor.properties"))
        {
            properties.store(output, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void jsonAndSchemaSelected(File jsonFile, File schemaFile, File settingsFile)
    {
        if (jsonFile != null && schemaFile != null)
        {
            // grab json from files and validate
            JsonFileReaderAndWriter reader = new JsonFileReaderAndWriterImpl();
            JsonNode json = reader.getJsonFromFile(jsonFile);
            JsonSchema schema = reader.getSchemaFromFile(schemaFile);
            if (reader.validateJsonWithSchema(json, schema))
            {
                // settings file is optional
                if (settingsFile != null)
                {
                    model.setSettings(reader.getJsonFromFile(settingsFile, Settings.class));
                }
                model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
            }
            else
            {
                view.cantValidateJson();
            }
    
        }
        else
        {
            view.selectJsonAndSchema();
        }
    
    
    }
    
    @Override
    public void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index)
    {
        model.moveItemToIndex(newParent, item, index);
    }
    
    @Override
    public void removeNode(String path)
    {
        model.removeNode(path);
    }
    
    @Override
    public void addNewNodeToArray(String path)
    {
        model.addNodeToArray(path);
    }
    
    @Override
    public void saveToFile()
    {
        JsonFileReaderAndWriter jsonWriter = new JsonFileReaderAndWriterImpl();
        jsonWriter.writeJsonToFile(readableModel.getRootJson(), readableModel.getCurrentJSONFile());
    
    }
    
    @Override
    public String getLastJsonPath()
    {
        return properties.getProperty(PROPERTY_LAST_JSON_PATH);
    }
    
    @Override
    public String getLastSchemaPath()
    {
        return properties.getProperty(PROPERTY_LAST_SCHEMA_PATH);
    }
    
    @Override
    public String getLastSettingsPath()
    {
        return properties.getProperty(PROPERTY_LAST_SETTINGS_PATH);
    }
    
    @Override
    public boolean getRememberPaths()
    {
        return "true".equalsIgnoreCase(properties.getProperty(PROPERTY_REMEMBER_PATHS));
    }
    
    
}
