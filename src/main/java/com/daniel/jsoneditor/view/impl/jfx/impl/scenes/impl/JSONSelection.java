package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.daniel.jsoneditor.controller.Controller;

import java.io.File;

public class JSONSelection extends SceneHandlerImpl
{
    private File selectedJson;
    
    private File selectedSchema;
    
    private File selectedSettings;
    
    private File lastDirectory;
    
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
            selectedJson = new File(rememberedJsonPath);
        }
        if (rememberFiles && rememberedSchemaPath != null)
        {
            selectedSchema = new File(rememberedSchemaPath);
        }
        if (rememberFiles && rememberedSettingsPath != null)
        {
            selectedSettings = new File(rememberedSettingsPath);
        }
        boolean rememberedRememberSettings = controller.getRememberPaths();
        Label jsonLabel = new Label("JSON to edit:");
        TextField jsonFileField = new TextField(rememberedJsonPath);
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
            selectedJson = fileChooser.showOpenDialog(stage);
            if (selectedJson != null)
            {
                jsonFileField.setText(selectedJson.getAbsolutePath());
                lastDirectory = selectedJson.getParentFile();
            }
        });
        HBox jsonBox = new HBox(jsonLabel, jsonFileField, jsonButton);
        
        Label schemaLabel = new Label("Schema:");
        TextField schemaFileField = new TextField(rememberedSchemaPath);
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
            selectedSchema = fileChooser.showOpenDialog(stage);
            if (selectedSchema != null)
            {
                schemaFileField.setText(selectedSchema.getAbsolutePath());
                lastDirectory = selectedSchema.getParentFile();
            }
        });
        HBox schemaBox = new HBox(schemaLabel, schemaFileField, schemaButton);
        Label settingsLabel = new Label("Settings:");
        TextField settingsFileField = new TextField(rememberedSettingsPath);
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
            selectedSettings = fileChooser.showOpenDialog(stage);
            if (selectedSettings != null)
            {
                settingsFileField.setText(selectedSettings.getAbsolutePath());
                lastDirectory = selectedSettings.getParentFile();
            }
        });
        HBox settingsBox = new HBox(settingsLabel, settingsFileField, settingsButton);
    
        CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberedRememberSettings);
        
        Button okButton = new Button("OK");
        okButton.setOnAction(e ->
        {
            controller.setFileProperties(rememberCheckBox.isSelected(), selectedJson.getAbsolutePath(), selectedSchema.getAbsolutePath(), selectedSettings.getAbsolutePath());
            controller.jsonAndSchemaSelected(selectedJson, selectedSchema, selectedSettings);
        });
        
        GridPane root = new GridPane();
        root.addRow(0, jsonBox);
        root.addRow(1, schemaBox);
        root.addRow(2, settingsBox);
        root.addRow(3, rememberCheckBox);
        root.addRow(4, okButton);
        
        return new Scene(root, 400, 200);
    }
}
