package com.daniel.jsoneditor.controller.impl;

import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import com.daniel.jsoneditor.controller.impl.json.VariableHelper;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonNodeMerger;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.settings.Settings;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.daniel.jsoneditor.view.impl.ViewImpl;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.referencing.SelectReferenceDialog;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.referencing.ReferenceType;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.referencing.ReferenceTypeDialog;
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
import java.util.Optional;
import java.util.Set;


public class ControllerImpl implements Controller, Observer
{
    
    private final WritableModel model;
    
    private final ReadableModel readableModel;
    
    private final View view;
    
    private final List<Subject> subjects;
    
    private final SettingsController settingsController;
    
    public ControllerImpl(WritableModel model, ReadableModel readableModel, Stage stage)
    {
        this.settingsController = new SettingsControllerImpl();
        this.model = model;
        this.readableModel = readableModel;
        this.subjects = new ArrayList<>();
        this.view = new ViewImpl(readableModel, this, stage);
        this.view.observe(this.readableModel.getForObservation());
    }
    
    @Override
    public SettingsController getSettingsController()
    {
        return settingsController;
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
        model.sendEvent(new Event(EventEnum.READ_JSON_AND_SCHEMA));
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
        
        if (!variables.isEmpty())
        {
            VariableReplacementDialog dialog = new VariableReplacementDialog();
            Map<String, String> replacements = dialog.showAndWait(variables);
            
            if (replacements != null)
            {
                return VariableHelper.replaceVariables(text, replacements);
            }
        }
        return text;
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
            model.sendEvent(new Event(EventEnum.IMPORT_SUCCESSFUL));
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
            List<String> pathsToExport = readableModel.getDependentPaths(nodeWithPath);
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
        model.sendEvent(new Event(EventEnum.EXPORT_SUCCESSFUL));
        
    }
    
    @Override
    public void removeNode(String path)
    {
        model.removeNode(path);
    }
    
    @Override
    public void addNewNodeToArray(String path)
    {
        // we check whether the array holds ReferencesToObjects or referenceable objects. In these cases, we do some additional steps
        
        if (false && readableModel.getReferenceToObject(path) != null)
        {
            ReferenceTypeDialog dialog = new ReferenceTypeDialog();
            Optional<ReferenceType> result = dialog.showAndWait();
            if (result.isPresent())
            {
                ReferenceType referenceType = result.get();
                if (ReferenceType.REFERENCE_TO_OBJECT.equals(referenceType))
                {
                    Optional<String> dialogResult = new SelectReferenceDialog(
                            readableModel.getReferenceableObjectInstances()).showAndWait();
                    if (dialogResult.isPresent())
                    {
                        String resultString = dialogResult.get();
                        if (ReferenceType.CREATE_NEW_REFERENCE.name().equals(resultString))
                        {
                            // show the dialog to create a new reference
                        } else {
                            // the user selected an existing object, so we now need to make a reference to that object and add that one to
                            // the array
                            
                            
                        }
                        
                    }
                }
                else if (ReferenceType.MANUAL_REFERENCE.equals(referenceType))
                {
                    // the user wants to manually add a reference so we simply create a new object
                    model.addNodeToArray(path);
                }
            }
        }
        else
        {
            if (false)
            {
                // TODO handle referenceable objects
            }
            else
            {
                // the array contains neither references nor objects
                model.addNodeToArray(path);
            }
        }
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
        model.sendEvent(new Event(EventEnum.SAVING_SUCCESSFUL));
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
    public void generateJson()
    {
    
    }
}
