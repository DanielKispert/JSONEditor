package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl;

import java.io.File;
import java.util.Optional;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.UIHandler;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.selection.FileSelectionBox;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class JSONSelectionScene extends SceneHandlerImpl
{
    private final SettingsController settingsController;

    private final JsonSchemaSelectionListener listener;

    private String selectedJsonPath;

    private String selectedSchemaPath;

    private String selectedSettingsPath;

    private File lastDirectory;

    private boolean remember;

    public JSONSelectionScene(UIHandler handler, SettingsController settingsController, ReadableModel model,
            JsonSchemaSelectionListener listener)
    {
        super(handler, null, model);
        this.settingsController = settingsController;
        this.listener = listener;
    }

    @Override
    public Scene getScene(Stage stage)
    {
        final boolean rememberFiles = settingsController.rememberPaths();
        final String rememberedJsonPath = settingsController.getLastJsonPath();
        final String rememberedSchemaPath = settingsController.getLastSchemaPath();
        final String rememberedSettingsPath = settingsController.getLastSettingsPath();
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
        final boolean rememberedRememberSettings = settingsController.rememberPaths();

        final FileSelectionBox jsonBox = new FileSelectionBox("JSON to edit:", selectedJsonPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        final FileSelectionBox schemaBox = new FileSelectionBox("Schema:", selectedSchemaPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        final FileSelectionBox settingsBox = new FileSelectionBox("Settings:", selectedSettingsPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        final CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberedRememberSettings);

        final Button okButton = new Button("OK");
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

        final VBox root = new VBox(10, jsonBox, schemaBox, settingsBox, rememberCheckBox, okButton);
        root.setPadding(new Insets(10));

        final Scene scene = new Scene(root, 700, 300);
        scene.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        return scene;
    }

    private void continueToEditor()
    {
        listener.onFilesSelected(new File(selectedJsonPath), new File(selectedSchemaPath), new File(selectedSettingsPath));
        settingsController.setFileProperties(remember, selectedJsonPath, selectedSchemaPath, selectedSettingsPath);
    }

    private void askToGenerateJson(Stage stage)
    {
        // Create an alert dialog to ask for the JSON path
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("JSON Path");
        alert.setHeaderText("No JSON path entered");
        alert.setContentText("Do you want to generate one?");
        final ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        final ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Apply the common CSS to the alert dialog
        final DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());

        final Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton)
        {
            // Create the file chooser with a default JSON path
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save As");
            fileChooser.setInitialDirectory(lastDirectory);
            fileChooser.setInitialFileName("newfile.json");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

            // Show the file chooser dialog
            final File selectedDirectory = fileChooser.showSaveDialog(stage);

            if (selectedDirectory != null)
            {
                // Get the selected file name and directory
                final String fileName = fileChooser.getInitialFileName();
                final File selectedFile = new File(selectedDirectory, fileName);
                // generate a JSON and save it in the selected file
                // TODO
            }
        }
    }
}
