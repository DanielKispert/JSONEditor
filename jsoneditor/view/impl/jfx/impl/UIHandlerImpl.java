package jsoneditor.view.impl.jfx.impl;

import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelection;
import jsoneditor.view.impl.jfx.impl.scenes.impl.MainEditor;

public class UIHandlerImpl implements UIHandler
{
    private final Controller controller;
    
    private final Stage stage;
    
    private final ReadableModel model;
    
    private MainEditor mainEditor;
    
    public UIHandlerImpl(Controller controller, Stage stage, ReadableModel model)
    {
        this.controller = controller;
        this.stage = stage;
        this.model = model;
    }
    
    @Override
    public void showSelectJsonAndSchema()
    {
        stage.setScene(new JSONSelection(controller, model).getScene(stage));
        stage.show();
    }
    
    @Override
    public void showMainEditor()
    {
        this.mainEditor = new MainEditor(controller, model);
        stage.setScene(mainEditor.getScene(stage));
        stage.show();
    }
    
    @Override
    public void updateEditorSceneWithSelectedJson()
    {
        if (mainEditor != null)
        {
            mainEditor.updateSelectedJson();
        }
    }
}
