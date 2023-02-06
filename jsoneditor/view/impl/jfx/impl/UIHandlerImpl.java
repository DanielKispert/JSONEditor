package jsoneditor.view.impl.jfx.impl;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.view.impl.jfx.UIHandler;
import jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelection;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;

import java.util.Optional;

public class UIHandlerImpl implements UIHandler
{
    private final Controller controller;
    
    private final Stage stage;
    
    private final ReadableModel model;
    
    private EditorScene editorScene;
    
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
        this.editorScene = new EditorScene(controller, model);
        stage.setScene(editorScene.getScene(stage));
        stage.show();
    }
    
    @Override
    public void updateEditorSceneWithSelectedJson()
    {
        if (editorScene != null)
        {
            editorScene.updateSelectedJson();
        }
    }
    
    @Override
    public void updateEditorSceneWithRemovedJson()
    {
        if (editorScene != null)
        {
            editorScene.handleRemovedSelection();
        }
        
    }
    
    public static void showConfirmDialog(Runnable onContinue, String text)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText(text);
    
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            onContinue.run();
        }
    }
}
