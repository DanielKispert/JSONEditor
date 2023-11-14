package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class VariableReplacementDialog
{
    public Map<String, String> showAndWait(Collection<String> variables)
    {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Replace Variables");
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        Map<String, TextField> fields = new HashMap<>();
        
        int row = 0;
        for (String variable : variables)
        {
            Label label = new Label(variable);
            TextField textField = new TextField("<" + variable + ">");
            fields.put(variable, textField);
            
            grid.addRow(row++, label, textField);
        }
        vbox.getChildren().add(grid);
        
        ScrollPane scroll = new ScrollPane(vbox);
        dialog.getDialogPane().setContent(scroll);
        
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK)
            {
                Map<String, String> result = new HashMap<>();
                for (String variable : variables)
                {
                    result.put(variable, fields.get(variable).getText());
                }
                return result;
            }
            return null;
        });
        
        Optional<Map<String, String>> result = dialog.showAndWait();
        
        return result.orElse(null);
    }
}