package com.daniel.jsoneditor.controller.impl;

import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import com.daniel.jsoneditor.controller.impl.json.VariableHelper;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonNodeMerger;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.settings.Settings;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.view.impl.ViewImpl;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.VariableReplacementDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.View;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


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
    
    private final List<Subject> subjects;
    
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
            JsonSchema schema = reader.getSchemaFromFileResolvingRefs(schemaFile);
            handleJsonValidation(json, schema, () -> {
                // settings file is optional
                if (settingsFile != null)
                {
                    Settings settingsFromFile = reader.getJsonFromFile(settingsFile, Settings.class, true);
                    if (settingsFromFile != null)
                    {
                        model.setSettings(settingsFromFile);
                    }
                }
                model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
            });
    
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
    public String resolveVariablesInJson(String text)
    {
        Set<String> variables = VariableHelper.findVariables(text);
    
        if (variables.size() > 0)
        {
            VariableReplacementDialog dialog = new VariableReplacementDialog();
            Map<String, String> replacements = dialog.showAndWait(variables);
        
            if (replacements != null)
            {
                return VariableHelper.replaceVariables(text, replacements);
            }
        }
        return null;
    }
    
    @Override
    public void importAtNode(String path, String content)
    {
        JsonFileReaderAndWriter jsonReader = new JsonFileReaderAndWriterImpl();
        JsonNode existingNodeAtPath = readableModel.getRootJson().at(path); // we intentionally ignore JsonNodeWithPath in case its null
        JsonNode contentNode = jsonReader.getNodeFromString(content);
        JsonNode mergedNode = JsonNodeMerger.createMergedNode(existingNodeAtPath, contentNode);
        JsonSchema schemaAtPath = readableModel.getSubschemaForPath(path);
        if (mergedNode != null && jsonReader.validateJsonWithSchema(mergedNode, schemaAtPath))
        {
            // the node exists and is valid for its current location
            model.setNode(path, mergedNode);
        }
    }
    
    @Override
    public void exportNode(String path)
    {
        // exporting a node does not require writing to the model, hence we only need the controller and the readable model
        JsonNodeWithPath nodeWithPath = readableModel.getNodeForPath(path);
        if (nodeWithPath != null)
        {
            String fileWithEnding = readableModel.getCurrentJSONFile().getName();
            int lastDotIndex = fileWithEnding.lastIndexOf(".");
            String fileWithoutEnding = (lastDotIndex != -1) ? fileWithEnding.substring(0, lastDotIndex) : fileWithEnding;
            String filename = fileWithoutEnding + "_export" + nodeWithPath.getPath().replace("/", "_") + ".json";
            exportJsonNode(filename, nodeWithPath.getNode());
        }
    }
    
    @Override
    public void exportNodeWithDependencies(String path)
    {
        JsonNodeWithPath nodeWithPath = readableModel.getNodeForPath(path);
        if (nodeWithPath != null)
        {
            // we want to export the node and all parent nodes of the node (but no other child nodes of the parent nodes)
            List<String> pathsToExport = readableModel.getDependentPaths(nodeWithPath);// TODO fill list with dependent nodes
            pathsToExport.add(readableModel.getNodeForPath(path).getPath());
        
            String fileWithEnding = readableModel.getCurrentJSONFile().getName();
            int lastDotIndex = fileWithEnding.lastIndexOf(".");
            String fileWithoutEnding = (lastDotIndex != -1) ? fileWithEnding.substring(0, lastDotIndex) : fileWithEnding;
            String filename = fileWithoutEnding + "_export_with_dependencies" + nodeWithPath.getPath().replace("/", "_") + ".json";
            exportJsonNode(filename, readableModel.getExportStructureForNodes(pathsToExport));
        }
    }
    
    private void exportJsonNode(String exportFilename, JsonNode node)
    {
        File directory = readableModel.getCurrentJSONFile().getParentFile();
        File exportFile = new File(directory, exportFilename);
        JsonFileReaderAndWriter writer = new JsonFileReaderAndWriterImpl();
        writer.writeJsonToFile(node, exportFile);
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
    public void sortArray(String path)
    {
        model.sortArray(path);
    }
    
    @Override
    public void duplicateArrayNode(String path)
    {
        model.duplicateArrayItem(path);
    }
    
    @Override
    public void saveToFile()
    {
        JsonFileReaderAndWriter jsonWriter = new JsonFileReaderAndWriterImpl();
        jsonWriter.writeJsonToFile(readableModel.getRootJson(), readableModel.getCurrentJSONFile());
    }
    
    @Override
    public void refreshFromFile()
    {
        // TODO we can add a dialog to ask the user if they're really sure here
        JsonFileReaderAndWriter reader = new JsonFileReaderAndWriterImpl();
        JsonNode json = reader.getJsonFromFile(readableModel.getCurrentJSONFile());
        handleJsonValidation(json, readableModel.getRootSchema(), () -> model.refreshJsonNode(json));
    }
    
    @Override
    public String searchForNode(String path, String value)
    {
        return readableModel.searchForNode(path, value);
    }
    
    private void handleJsonValidation(JsonNode json, JsonSchema schema, Runnable onSuccess)
    {
        JsonFileReaderAndWriter reader = new JsonFileReaderAndWriterImpl();
        if (reader.validateJsonWithSchema(json, schema))
        {
            onSuccess.run();
        }
        else
        {
            view.cantValidateJson();
        }
    }
    
    @Override
    public void openNewJson()
    {
        launchFinished();
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
    
    @Override
    public void generateJson()
    {
    
    
    }
}
