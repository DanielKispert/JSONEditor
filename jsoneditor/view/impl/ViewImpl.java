package jsoneditor.view.impl;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.Event;
import jsoneditor.view.View;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.UIHandlerImpl;

import java.util.ArrayList;
import java.util.List;

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
        Alert alert = new Alert(Alert.AlertType.ERROR, "Can't validate JSON using selected Schema!", ButtonType.OK);
        alert.showAndWait();
    }
}
