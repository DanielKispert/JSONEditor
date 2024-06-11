package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import java.io.File;
import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
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
        // JSON
        Label jsonLabel = new Label("JSON to edit:");
        TextField jsonFileField = new TextField(selectedJsonPath);
        jsonFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedJsonPath = newValue);
        HBox jsonBox = getJsonBox(stage, jsonFileField, jsonLabel);
        // SCHEMA
        Label schemaLabel = new Label("Schema:");
        TextField schemaFileField = new TextField(selectedSchemaPath);
        schemaFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedSchemaPath = newValue);
        HBox schemaBox = getSchemaBox(stage, schemaFileField, schemaLabel);
        // SETTINGS
        Label settingsLabel = new Label("Settings:");
        TextField settingsFileField = new TextField(selectedSettingsPath);
        settingsFileField.textProperty().addListener((observable, oldValue, newValue) -> selectedSettingsPath = newValue);
        HBox settingsBox = getSettingsBox(stage, settingsFileField, settingsLabel);
        
        CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberedRememberSettings);
        
        Button okButton = new Button("OK");
        okButton.setOnAction(e ->
        {
            remember = rememberCheckBox.isSelected();
            if (selectedJsonPath == null || selectedJsonPath.isEmpty())
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
        
        Scene scene = new Scene(root, 400, 200);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        return scene;
    }
    
    private HBox getSettingsBox(Stage stage, TextField settingsFileField, Label settingsLabel)
    {
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
        return new HBox(settingsLabel, settingsFileField, settingsButton);
    }
    
    private HBox getSchemaBox(Stage stage, TextField schemaFileField, Label schemaLabel)
    {
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
        return new HBox(schemaLabel, schemaFileField, schemaButton);
    }
    
    private HBox getJsonBox(Stage stage, TextField jsonFileField, Label jsonLabel)
    {
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
        return new HBox(jsonLabel, jsonFileField, jsonButton);
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
