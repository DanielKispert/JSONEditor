package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;

import java.io.File;
import java.util.Optional;

public class JSONSelection extends SceneHandlerImpl
{
    private String selectedJsonPath;
    
    private String selectedSchemaPath;
    
    private String selectedSettingsPath;
    
    private File lastDirectory;
    
    private boolean remember;
    
    public JSONSelection(Controller controller, ReadableModel model)
    {
        super(controller, model);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        boolean rememberFiles = controller.getRememberPaths();
        String rememberedJsonPath = controller.getLastJsonPath();
        String rememberedSchemaPath = controller.getLastSchemaPath();
        String rememberedSettingsPath = controller.getLastSettingsPath();
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
        boolean rememberedRememberSettings = controller.getRememberPaths();
        // JSON
        Label jsonLabel = new Label("JSON to edit:");
        TextField jsonFileField = new TextField(rememberedJsonPath);
        jsonFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedJsonPath = newValue);
        Button jsonButton = new Button("Select JSON");
        jsonButton.setOnAction(e ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select JSON file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            if (lastDirectory != null)
            {
                fileChooser.setInitialDirectory(lastDirectory);
            }
            File selectedJson = fileChooser.showOpenDialog(stage);
            if (selectedJson != null)
            {
                selectedJsonPath = selectedJson.getAbsolutePath();
                jsonFileField.setText(selectedJsonPath);
                lastDirectory = selectedJson.getParentFile();
            }
        });
        HBox jsonBox = new HBox(jsonLabel, jsonFileField, jsonButton);
        // SCHEMA
        Label schemaLabel = new Label("Schema:");
        TextField schemaFileField = new TextField(rememberedSchemaPath);
        schemaFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedSchemaPath = newValue);
        Button schemaButton = new Button("Select Schema");
        schemaButton.setOnAction(e ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Schema file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            if (lastDirectory != null)
            {
                fileChooser.setInitialDirectory(lastDirectory);
            }
            File selectedSchema = fileChooser.showOpenDialog(stage);
            if (selectedSchema != null)
            {
                selectedSchemaPath = selectedSchema.getAbsolutePath();
                schemaFileField.setText(selectedSchemaPath);
                lastDirectory = selectedSchema.getParentFile();
            }
        });
        HBox schemaBox = new HBox(schemaLabel, schemaFileField, schemaButton);
        // SETTINGS
        Label settingsLabel = new Label("Settings:");
        TextField settingsFileField = new TextField(rememberedSettingsPath);
        settingsFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedSettingsPath = newValue);
        Button settingsButton = new Button("Select Settings");
        settingsButton.setOnAction(e ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Settings file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            if (lastDirectory != null)
            {
                fileChooser.setInitialDirectory(lastDirectory);
            }
            File selectedSettings = fileChooser.showOpenDialog(stage);
            if (selectedSettings != null)
            {
                selectedSettingsPath = selectedSettings.getAbsolutePath();
                settingsFileField.setText(selectedSettingsPath);
                lastDirectory = selectedSettings.getParentFile();
            }
        });
        HBox settingsBox = new HBox(settingsLabel, settingsFileField, settingsButton);
    
        CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberedRememberSettings);
        
        Button okButton = new Button("OK");
        okButton.setOnAction(e ->
        {
            remember = rememberCheckBox.isSelected();
            if (selectedJsonPath.isEmpty())
            {
                askToGenerateJson(stage);
            }
            else
            {
                continueToEditor();
            }
    
        });
        
        GridPane root = new GridPane();
        root.addRow(0, jsonBox);
        root.addRow(1, schemaBox);
        root.addRow(2, settingsBox);
        root.addRow(3, rememberCheckBox);
        root.addRow(4, okButton);
        
        return new Scene(root, 400, 200);
    }
    
    private void continueToEditor()
    {
        controller.setFileProperties(remember, selectedJsonPath, selectedSchemaPath, selectedSettingsPath);
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
