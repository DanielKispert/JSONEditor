package jsoneditor.view.impl.jfx.impl.scenes.impl;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;

import java.io.File;

public class JSONSelection extends SceneHandlerImpl
{
    private File selectedJson;
    
    private File selectedSchema;
    
    public JSONSelection(Controller controller)
    {
        super(controller);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        Label jsonLabel = new Label("JSON to edit:");
        TextField jsonFileField = new TextField();
        Button jsonButton = new Button("Select JSON");
        jsonButton.setOnAction(e ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select JSON file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            selectedJson = fileChooser.showOpenDialog(stage);
            if (selectedJson != null)
            {
                jsonFileField.setText(selectedJson.getAbsolutePath());
            }
        });
        HBox jsonBox = new HBox(jsonLabel, jsonFileField, jsonButton);
        
        Label schemaLabel = new Label("Schema:");
        TextField schemaFileField = new TextField();
        Button schemaButton = new Button("Select Schema");
        schemaButton.setOnAction(e ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Schema file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            selectedSchema = fileChooser.showOpenDialog(stage);
            if (selectedSchema != null)
            {
                schemaFileField.setText(selectedSchema.getAbsolutePath());
            }
        });
        HBox schemaBox = new HBox(schemaLabel, schemaFileField, schemaButton);
        
        Button okButton = new Button("OK");
        okButton.setOnAction(e ->
        {
            controller.jsonAndSchemaSelected(selectedJson, selectedSchema);
        });
        
        GridPane root = new GridPane();
        root.addRow(0, jsonBox);
        root.addRow(1, schemaBox);
        root.addRow(2, okButton);
        
        return new Scene(root, 400, 200);
    }
}
