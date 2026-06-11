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

    public JSONSelectionScene(final UIHandler handler, final SettingsController settingsController, final ReadableModel model,
            final JsonSchemaSelectionListener listener)
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

        final FileSelectionBox jsonBox = new FileSelectionBox("JSON to edit:", selectedJsonPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        final FileSelectionBox schemaBox = new FileSelectionBox("Schema:", selectedSchemaPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        final FileSelectionBox settingsBox = new FileSelectionBox("Settings:", selectedSettingsPath, stage,
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        final CheckBox rememberCheckBox = new CheckBox("Remember");
        rememberCheckBox.setSelected(rememberFiles);

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
        final File schemaFile = selectedSchemaPath != null ? new File(selectedSchemaPath) : null;
        final File settingsFile = selectedSettingsPath != null ? new File(selectedSettingsPath) : null;
        listener.onFilesSelected(new File(selectedJsonPath), schemaFile, settingsFile);
        settingsController.setFileProperties(remember, selectedJsonPath, selectedSchemaPath, selectedSettingsPath);
    }

    private void askToGenerateJson(Stage stage)
    {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("JSON Path");
        alert.setHeaderText("No JSON path entered");
        alert.setContentText("Do you want to generate one?");
        final ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        final ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        final DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());

        final Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton)
        {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save As");
            fileChooser.setInitialDirectory(lastDirectory);
            fileChooser.setInitialFileName("newfile.json");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

            final File selectedDirectory = fileChooser.showSaveDialog(stage);

            if (selectedDirectory != null)
            {
                final String fileName = fileChooser.getInitialFileName();
                final File selectedFile = new File(selectedDirectory, fileName);
                // TODO: implement save-generated-JSON-from-schema flow
            }
        }
    }
}
