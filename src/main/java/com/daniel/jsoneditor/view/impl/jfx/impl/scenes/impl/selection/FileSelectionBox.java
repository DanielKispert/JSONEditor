package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.selection;

import java.io.File;

import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class FileSelectionBox extends HBox
{
    private final Label titleLabel;
    
    private final Label fileLabel;
    
    private final Button selectButton;
    
    public FileSelectionBox(String title, String initialFilePath, Stage stage, FileChooser.ExtensionFilter filter)
    {
        this.titleLabel = new Label(title);
        this.fileLabel = new Label(initialFilePath);
        this.selectButton = new Button();
        ButtonHelper.setButtonImage(selectButton, "/icons/material/darkmode/outline_open_in_new_white_24dp.png");
        
        this.fileLabel.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-alignment: center-right;");
        HBox.setHgrow(fileLabel, Priority.ALWAYS);
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        
        this.selectButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select " + title);
            fileChooser.getExtensionFilters().add(filter);
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null)
            {
                this.fileLabel.setText(selectedFile.getAbsolutePath());
            }
        });
        
        this.getChildren().addAll(titleLabel, fileLabel, selectButton);
        HBox.setHgrow(fileLabel, Priority.ALWAYS);
    }
    
    public String getFilePath()
    {
        return fileLabel.getText();
    }
}
