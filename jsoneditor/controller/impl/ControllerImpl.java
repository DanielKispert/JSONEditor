package jsoneditor.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.controller.impl.json.JsonReader;
import jsoneditor.controller.impl.json.impl.JsonReaderImpl;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.ReadableState;
import jsoneditor.model.WritableModel;
import jsoneditor.model.json.JsonNodeWithPath;
import jsoneditor.model.observe.Observer;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.Event;
import jsoneditor.view.View;
import jsoneditor.view.impl.ViewImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControllerImpl implements Controller, Observer
{
    private final WritableModel model;
    
    private final ReadableState readableState;
    
    private final View view;
    
    private Event currentState;
    
    private List<Subject> subjects;
    
    public ControllerImpl(WritableModel model, ReadableModel readableModel, Stage stage) {
        this.model = model;
        this.readableState = readableModel;
        this.currentState = null;
        this.subjects = new ArrayList<>();
        this.view = new ViewImpl(readableModel, this, stage);
        this.view.observe(readableState.getForObservation());
    }
    
    
    @Override
    public void update()
    {
        this.currentState = readableState.getCurrentState();
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
    public void jsonAndSchemaSelected(File jsonFile, File schemaFile)
    {
        if (jsonFile != null && schemaFile != null)
        {
            // grab json from files and validate
            JsonReader reader = new JsonReaderImpl();
            JsonNode json = reader.getJsonFromFile(jsonFile);
            JsonSchema schema = reader.getSchemaFromFile(schemaFile);
            if (reader.validateJsonWithSchema(json, schema))
            {
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
    public void chooseNodeFromNavbar(JsonNodeWithPath node)
    {
        model.selectJsonNode(node);
    }
    
    @Override
    public void removeNodeFromArray(JsonNode node)
    {
        model.removeNodeFromArray(node);
    }
    
    @Override
    public void removeSelectedNode()
    {
        // TODO check if the user tries to remove the root node and prevent
        model.removeSelectedNode();
    }
}
