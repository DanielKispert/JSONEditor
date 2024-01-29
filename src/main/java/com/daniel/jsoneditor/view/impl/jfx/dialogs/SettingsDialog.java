package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class SettingsDialog extends Dialog<Void>
{
    private boolean tmpHideEmptyColumns;
    
    private boolean tmpRenameReferences;
    
    private final SettingsController settingsController;
    
    public SettingsDialog(SettingsController controller)
    {
        super();
        this.settingsController = controller;
        
        this.tmpHideEmptyColumns = settingsController.hideEmptyColumns();
        this.tmpRenameReferences = settingsController.renameReferencesWhenRenamingObject();
        
        setTitle("Settings");
        getDialogPane().getButtonTypes().setAll(new ButtonType("Save", ButtonType.OK.getButtonData()), ButtonType.CANCEL);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        TabPane tabPane = createTabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        vbox.setMinSize(500, 300);
        vbox.getChildren().add(tabPane);
        getDialogPane().setContent(vbox);
        
        setResultConverter(button -> {
            if (button.getButtonData() == ButtonType.OK.getButtonData())
            {
                settingsController.setHideEmptyColumns(tmpHideEmptyColumns);
                settingsController.setRenameReferencesWhenRenamingObject(tmpRenameReferences);
            }
            return null;
        });
    }
    
    private TabPane createTabPane()
    {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Automation Tab
        Tab automationTab = new Tab("Automation");
        VBox automationBox = new VBox(createAutomationSettings());
        automationTab.setContent(automationBox);
        
        // Display Tab
        Tab displayTab = new Tab("Display");
        VBox displayBox = new VBox(createDisplaySettings());
        displayTab.setContent(displayBox);
        
        tabs.getTabs().addAll(automationTab, displayTab);
        
        return tabs;
    }
    
    private CheckBox createAutomationSettings()
    {
        CheckBox editReferencesCheckBox = new CheckBox("Change references when renaming a referenceable object");
        editReferencesCheckBox.setSelected(tmpRenameReferences);
        editReferencesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> tmpRenameReferences = newValue);
        return editReferencesCheckBox;
    }
    
    private CheckBox createDisplaySettings()
    {
        CheckBox hideEmptyColumnsCheckBox = new CheckBox("Hide empty, non-required columns in arrays");
        hideEmptyColumnsCheckBox.setSelected(tmpHideEmptyColumns);
        hideEmptyColumnsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> tmpHideEmptyColumns = newValue);
        return hideEmptyColumnsCheckBox;
    }
}
