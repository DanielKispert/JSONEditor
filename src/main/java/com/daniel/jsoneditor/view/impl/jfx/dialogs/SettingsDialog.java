package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.daniel.jsoneditor.controller.settings.SettingsController;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class SettingsDialog extends ThemedDialog<Void>
{
    private boolean tmpHideEmptyColumns;
    
    private boolean tmpRenameReferences;
    
    private boolean tmpDebugMode;
    
    private boolean tmpLogGraphRequests;
    
    private String tmpClusterShape;
    
    private final SettingsController settingsController;
    
    public SettingsDialog(SettingsController controller)
    {
        super();
        this.settingsController = controller;
        
        this.tmpHideEmptyColumns = settingsController.hideEmptyColumns();
        this.tmpRenameReferences = settingsController.renameReferencesWhenRenamingObject();
        this.tmpDebugMode = settingsController.isDebugMode();
        this.tmpLogGraphRequests = settingsController.isLogGraphRequests();
        this.tmpClusterShape = settingsController.getClusterShape();
        
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
                settingsController.setDebugMode(tmpDebugMode);
                settingsController.setLogGraphRequests(tmpLogGraphRequests);
                settingsController.setClusterShape(tmpClusterShape);
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
        VBox displayBox = new VBox(createDisplaySettings(), createClusterShapeSettings());
        displayTab.setContent(displayBox);
        
        // Debug Tab
        Tab debugTab = new Tab("Debug");
        VBox debugBox = new VBox(createDebugToastsSetting(), createLogGraphRequestsSettings());
        debugTab.setContent(debugBox);
        
        tabs.getTabs().addAll(automationTab, displayTab, debugTab);
        
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
    
    private CheckBox createDebugToastsSetting()
    {
        CheckBox debugModeCheckBox = new CheckBox("Show toast notification on model changes");
        debugModeCheckBox.setSelected(tmpDebugMode);
        debugModeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> tmpDebugMode = newValue);
        return debugModeCheckBox;
    }
    
    private CheckBox createLogGraphRequestsSettings()
    {
        CheckBox logGraphRequestsCheckBox = new CheckBox("Log graph requests");
        logGraphRequestsCheckBox.setSelected(tmpLogGraphRequests);
        logGraphRequestsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> tmpLogGraphRequests = newValue);
        return logGraphRequestsCheckBox;
    }
    
    private HBox createClusterShapeSettings()
    {
        HBox box = new HBox();
        ComboBox<String> clusterShapeComboBox = new ComboBox<>();
        clusterShapeComboBox.getItems().addAll("star", "triangle", "square", "hexagon");
        clusterShapeComboBox.setValue(tmpClusterShape);
        clusterShapeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> tmpClusterShape = newValue);
        Label title = new Label("Cluster Shape: ");
        box.getChildren().addAll(title, clusterShapeComboBox);
        return box;
    }
}
