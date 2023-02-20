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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControllerImpl implements Controller, Observer
{
    private final WritableModel model;
    
    private final ReadableModel readableModel;
    
    private final View view;
    
    private Event currentState;
    
    private List<Subject> subjects;
    
    public ControllerImpl(WritableModel model, ReadableModel readableModel, Stage stage) {
        this.model = model;
        this.readableModel = readableModel;
        this.currentState = null;
        this.subjects = new ArrayList<>();
        this.view = new ViewImpl(readableModel, this, stage);
        this.view.observe(this.readableModel.getForObservation());
    }
    
    
    @Override
    public void update()
    {
        this.currentState = readableModel.getCurrentState();
    }
    
    @Override
    public void observe(Subject subjectToObserve)
    {
        subjects.add(subjectToObserve);
        subjectToObserve.registerObserver(this);
        
    }
    
    @Override
    public void searchForNode(String path, String value)
    {
        model.searchForNode(path, value);
    }
    
    @Override
    public void launchFinished()
    {
        model.sendEvent(Event.READ_JSON_AND_SCHEMA);
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
    public void chooseNodeFromNavbar(String path)
    {
        model.selectJsonNode(path);
    }
    
    @Override
    public void moveItemToIndex(JsonNodeWithPath item, int index)
    {
        model.moveItemToIndex(item, index);
    }
    
    @Override
    public void removeNodeFromArray(JsonNode node)
    {
        model.removeNodeFromSelectedArray(node);
    }
    
    @Override
    public void addNewNodeToSelectedArray()
    {
        model.addNodeToSelectedArray();
    }
    
    @Override
    public void removeSelectedNode()
    {
        // TODO check if the user tries to remove the root node and prevent
        model.removeSelectedNode();
    }
    
    @Override
    public void saveToFile()
    {
        JsonFileReaderAndWriter jsonWriter = new JsonFileReaderAndWriterImpl();
        jsonWriter.writeJsonToFile(readableModel.getRootJson(), readableModel.getCurrentJSONFile());
    
    }
}
