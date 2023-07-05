package com.daniel.jsoneditor.view.impl;

import com.daniel.jsoneditor.model.statemachine.impl.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.view.View;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.UIHandlerImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViewImpl implements View
{
    private List<Subject> subjects;
    
    private final ReadableModel model;
    
    private final Controller controller;
    
    private final UIHandler uiHandler;
    
    public ViewImpl(ReadableModel model, Controller controller, Stage stage)
    {
        this.subjects = new ArrayList<>();
        this.uiHandler = new UIHandlerImpl(controller, stage, model);
        this.controller = controller;
        this.model = model;
    }
    
    
    @Override
    public void update()
    {
        Event newState = model.getCurrentState();
        switch (newState)
        {
            case LAUNCHING:
                controller.launchFinished();
                break;
            case READ_JSON_AND_SCHEMA:
                uiHandler.showSelectJsonAndSchema();
                break;
            case MAIN_EDITOR:
                uiHandler.showMainEditor();
                break;
            case UPDATED_SELECTED_JSON_NODE:
                uiHandler.updateEditorSceneWithSelectedJson();
                break;
            case UPDATED_JSON_STRUCTURE:
                uiHandler.updateEditorSceneWithUpdatedStructure();
                break;
            case REMOVED_SELECTED_JSON_NODE:
                uiHandler.updateEditorSceneWithRemovedJson();
                break;
            case MOVED_CHILD_OF_SELECTED_JSON_NODE:
                uiHandler.updateEditorSceneWithMovedJson();
                break;
            
        }
    
    }
    
    @Override
    public void observe(Subject subjectToObserve)
    {
        subjectToObserve.registerObserver(this);
        subjects.add(subjectToObserve);
    }
    
    @Override
    public void cantValidateJson()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Can't validate JSON using selected Schema!", ButtonType.OK);
        alert.showAndWait();
    }
    
    @Override
    public void selectJsonAndSchema()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Select a JSON and a Schema!", ButtonType.OK);
        alert.showAndWait();
    }
}
