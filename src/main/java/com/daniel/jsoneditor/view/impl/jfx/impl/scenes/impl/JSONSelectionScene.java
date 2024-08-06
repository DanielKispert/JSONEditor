package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import java.io.File;
import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.selection.FileSelectionBox;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class JSONSelectionScene extends SceneHandlerImpl
{
    private String selectedJsonPath;
    
    private String selectedSchemaPath;
    
    private String selectedSettingsPath;
    
    private File lastDirectory;
    
    private boolean remember;
    
    public JSONSelectionScene(UIHandler handler, Controller controller, ReadableModel model)
    {
        super(handler, controller, model);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        SettingsController settingsController = controller.getSettingsController();
        boolean rememberFiles = settingsController.rememberPaths();
        String rememberedJsonPath = settingsController.getLastJsonPath();
        String rememberedSchemaPath = settingsController.getLastSchemaPath();
        String rememberedSettingsPath = settingsController.getLastSettingsPath();
        if (rememberFiles && rememberedJsonPath != null)
        {
            selectedJsonPath = rememberedJsonPath;
        }
        if (rememberFiles && rememberedSchemaPath != null)
        {
            selectedSchemaPath = rememberedSchemaPath;
        }
        if (rememberFiles && rememberedSettingsPath != null)
        {
            selectedSettingsPath = rememberedSettingsPath;
        }
        boolean rememberedRememberSettings = settingsController.rememberPaths();
        
        FileSelectionBox jsonBox = new FileSelectionBox("JSON to edit:", selectedJsonPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        FileSelectionBox schemaBox = new FileSelectionBox("Schema:", selectedSchemaPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        FileSelectionBox settingsBox = new FileSelectionBox("Settings:", selectedSettingsPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        
        CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberedRememberSettings);
        
        Button okButton = new Button("OK");
        okButton.setOnAction(e ->
        {
            remember = rememberCheckBox.isSelected();
            selectedJsonPath = jsonBox.getFilePath();
            selectedSchemaPath = schemaBox.getFilePath();
            selectedSettingsPath = settingsBox.getFilePath();
            if (selectedJsonPath == null || selectedJsonPath.isEmpty())
            {
                askToGenerateJson(stage);
            }
            else
            {
                continueToEditor();
            }
    
        });
        
        VBox root = new VBox(10, jsonBox, schemaBox, settingsBox, rememberCheckBox, okButton);
        root.setPadding(new Insets(10));
        
        Scene scene = new Scene(root, 700, 300);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        return scene;
    }
    
    private void continueToEditor()
    {
        controller.getSettingsController().setFileProperties(remember, selectedJsonPath, selectedSchemaPath, selectedSettingsPath);
        controller.jsonAndSchemaSelected(new File(selectedJsonPath), new File(selectedSchemaPath), new File(selectedSettingsPath));
    }
    
    private void askToGenerateJson(Stage stage)
    {
        // Create an alert dialog to ask for the JSON path
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("JSON Path");
        alert.setHeaderText("No JSON path entered");
        alert.setContentText("Do you want to generate one?");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == yesButton)
        {
            // Create the file chooser with a default JSON path
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save As");
            fileChooser.setInitialDirectory(lastDirectory);
            fileChooser.setInitialFileName("newfile.json");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            
            // Show the file chooser dialog
            File selectedDirectory = fileChooser.showSaveDialog(stage);
            
            if (selectedDirectory != null)
            {
                // Get the selected file name and directory
                String fileName = fileChooser.getInitialFileName();
                File selectedFile = new File(selectedDirectory, fileName);
                // generate a JSON and save it in the selected file
                // TODO
                
                
                
            }
        }
    }
}
