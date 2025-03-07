package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.controller.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DeleteAllButton extends Button
{
    private final Controller controller;
    private final Supplier<List<String>> pathsSupplier;
    
    public DeleteAllButton(Controller controller, Supplier<List<String>> pathsSupplier)
    {
        super();
        this.controller = controller;
        this.pathsSupplier = pathsSupplier;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_delete_all_white_24dp.png");
        setOnAction(actionEvent -> showConfirmationDialog());
    }
    
    private void showConfirmationDialog()
    {
        List<String> paths = pathsSupplier.get();
        if (paths.isEmpty())
        {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete the following items?");
        
        TextArea textArea = new TextArea(String.join("\n", paths));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        VBox dialogPaneContent = new VBox();
        dialogPaneContent.getChildren().add(textArea);
        alert.getDialogPane().setContent(dialogPaneContent);
        
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style_darkmode.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            deleteAllVisibleItems(paths);
        }
    }
    
    private void deleteAllVisibleItems(List<String> paths) {
        for (String path : paths) {
            controller.removeNode(path);
        }
    }
}
