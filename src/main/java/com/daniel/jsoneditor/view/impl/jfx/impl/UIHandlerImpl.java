package com.daniel.jsoneditor.view.impl.jfx.impl;

import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.impl.EditorDimensions;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.JSONSelectionScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import com.daniel.jsoneditor.view.impl.jfx.toast.impl.ToastImpl;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Screen;
import javafx.stage.Stage;


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
        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            EditorDimensions oldDimensions = controller.getSettingsController().getEditorDimensions();
            controller.getSettingsController().setEditorDimensions(oldDimensions.getWidth(), oldDimensions.getHeight(), newValue);
        });
        
    }
    
    @Override
    public void showSelectJsonAndSchema()
    {
        stage.setScene(new JSONSelectionScene(this, controller, model).getScene(stage));
        stage.setWidth(700);
        stage.setHeight(300);
        stage.show();
    }
    
    @Override
    public void showMainEditor()
    {
        EditorDimensions dimensions = controller.getSettingsController().getEditorDimensions();
        stage.setMaximized(dimensions.isMaximized()); //start maximized if the editor was maximized last
        stage.setWidth(dimensions.getWidth());
        stage.setHeight(dimensions.getHeight());
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - dimensions.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - dimensions.getHeight()) / 2);
        this.editorScene = new EditorScene(this, controller, model);
        stage.setScene(editorScene.getScene(stage));
        stage.show();
    }
    
    @Override
    public void handleAddedArrayItem(String pathOfArrayItem)
    {
        if (editorScene != null)
        {
            editorScene.handleAddedArrayItem(pathOfArrayItem);
        }
    }
    
    @Override
    public void handleAddedReferenceableObject(String pathOfObject)
    {
        if (editorScene != null)
        {
            editorScene.handleAddedReferenceableObject(pathOfObject);
        }
        
    }
    
    @Override
    public void updateEditorSceneWithSelectedJson()
    {
        if (editorScene != null)
        {
            editorScene.updateEverything();
        }
    }
    
    @Override
    public void updateEditorSceneWithRemovedJson()
    {
        if (editorScene != null)
        {
            editorScene.updateEverything();
        }
    }
    
    @Override
    public void updateEditorSceneWithUpdatedStructure()
    {
        if (editorScene != null)
        {
            editorScene.updateEverything();
        }
    }
    
    @Override
    public void updateEditorSceneWithMovedJson()
    {
        if (editorScene != null)
        {
            editorScene.handleMovedSelection();
        }
    }
    
    @Override
    public void showToast(Toasts toast)
    {
        new ToastImpl().show(stage, toast.getMessage(), toast.getColor(), toast.getDuration());
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
