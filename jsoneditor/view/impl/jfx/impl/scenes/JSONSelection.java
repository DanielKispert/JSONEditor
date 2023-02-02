package jsoneditor.view.impl.jfx.impl.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class JSONSelection
{
    
    private TextField jsonFileField;
    private TextField schemaFileField;
    
    public Scene getScene(Stage stage) {
        Label jsonLabel = new Label("JSON to edit:");
        jsonFileField = new TextField();
        Button jsonButton = new Button("Select JSON");
        jsonButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select JSON file");
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                jsonFileField.setText(selectedFile.getAbsolutePath());
            }
        });
        HBox jsonBox = new HBox(jsonLabel, jsonFileField, jsonButton);
    
        Label schemaLabel = new Label("Schema:");
        schemaFileField = new TextField();
        Button schemaButton = new Button("Select Schema");
        schemaButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Schema file");
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                schemaFileField.setText(selectedFile.getAbsolutePath());
            }
        });
        HBox schemaBox = new HBox(schemaLabel, schemaFileField, schemaButton);
    
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
        
        });
    
        GridPane root = new GridPane();
        root.addRow(0, jsonBox);
        root.addRow(1, schemaBox);
        root.addRow(2, okButton);
    
        return new Scene(root, 400, 200);
    }
}
