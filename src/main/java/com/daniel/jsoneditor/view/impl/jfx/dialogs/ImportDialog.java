package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ImportDialog extends Dialog<String>
{
    
    private final TextArea textArea;
    
    private final FileChooser fileChooser;
    
    public ImportDialog(Stage ownerStage)
    {
        super();
        
        initOwner(ownerStage);
        initStyle(StageStyle.DECORATED);
        setResizable(true);
        
        setTitle("Import Content");
        
        textArea = new TextArea();
        textArea.setWrapText(true);
    
        ButtonType pasteFromFileButtonType = new ButtonType("Paste from File", ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
        
        getDialogPane().getButtonTypes().addAll(cancelButtonType,pasteFromFileButtonType, importButtonType);
        
        Button pasteFromFileButton = (Button) getDialogPane().lookupButton(pasteFromFileButtonType);
        pasteFromFileButton.addEventFilter(ActionEvent.ACTION, event -> {
            pasteFromFile();
            event.consume(); // for not closing the dialog
        });
        
        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setOnAction(e -> handleButtonCancel());
        
        Button importButton = (Button) getDialogPane().lookupButton(importButtonType);
        importButton.setOnAction(e -> handleButtonConfirm());
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(pasteFromFileButton, cancelButton, importButton);
        
        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(textArea, buttonBox);
        dialogContent.setPadding(new Insets(10));
        // Text area needs to resize with the dialog to offer maximum space
        textArea.prefWidthProperty().bind(dialogContent.widthProperty());
        textArea.prefHeightProperty().bind(dialogContent.heightProperty());
        
        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(dialogContent);
        
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        setResultConverter(dialogButton -> {
            if (dialogButton == importButtonType)
            {
                return textArea.getText();
            }
            return null;
        });
    }
    
    private void pasteFromFile()
    {
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            try
            {
                textArea.setText(Files.readString(selectedFile.toPath()));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void handleButtonCancel()
    {
        setResult(null);
        close();
    }
    
    private void handleButtonConfirm()
    {
        setResult(textArea.getText());
        close();
    }
}
