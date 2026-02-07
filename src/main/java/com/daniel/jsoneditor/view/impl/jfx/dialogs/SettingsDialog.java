package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.daniel.jsoneditor.controller.mcp.McpController;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
    
    private boolean tmpMcpServerEnabled;
    
    private int tmpMcpServerPort;
    
    private final SettingsController settingsController;
    
    private final McpController mcpController;
    
    private Label mcpStatusLabel;
    
    private Button mcpToggleButton;
    
    public SettingsDialog(SettingsController settingsController, McpController mcpController)
    {
        super();
        this.settingsController = settingsController;
        this.mcpController = mcpController;
        
        this.tmpHideEmptyColumns = settingsController.hideEmptyColumns();
        this.tmpRenameReferences = settingsController.renameReferencesWhenRenamingObject();
        this.tmpDebugMode = settingsController.isDebugMode();
        this.tmpLogGraphRequests = settingsController.isLogGraphRequests();
        this.tmpClusterShape = settingsController.getClusterShape();
        this.tmpMcpServerEnabled = settingsController.isMcpServerEnabled();
        this.tmpMcpServerPort = settingsController.getMcpServerPort();
        
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
                settingsController.setMcpServerEnabled(tmpMcpServerEnabled);
                settingsController.setMcpServerPort(tmpMcpServerPort);
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
        
        // MCP Tab
        Tab mcpTab = new Tab("MCP");
        VBox mcpBox = new VBox(10);
        mcpBox.setPadding(new Insets(10));
        mcpBox.getChildren().addAll(createMcpEnabledSetting(), createMcpPortSetting(), createMcpServerControls());
        mcpTab.setContent(mcpBox);
        
        tabs.getTabs().addAll(automationTab, displayTab, debugTab, mcpTab);
        
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
    
    private CheckBox createMcpEnabledSetting()
    {
        CheckBox mcpEnabledCheckBox = new CheckBox("Enable MCP Server on startup");
        mcpEnabledCheckBox.setSelected(tmpMcpServerEnabled);
        mcpEnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> tmpMcpServerEnabled = newValue);
        return mcpEnabledCheckBox;
    }
    
    private HBox createMcpPortSetting()
    {
        HBox box = new HBox(10);
        Label title = new Label("MCP Server Port:");
        Spinner<Integer> portSpinner = new Spinner<>();
        portSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 65535, tmpMcpServerPort));
        portSpinner.setEditable(true);
        portSpinner.setPrefWidth(100);
        portSpinner.valueProperty().addListener((observable, oldValue, newValue) -> tmpMcpServerPort = newValue);
        box.getChildren().addAll(title, portSpinner);
        return box;
    }
    
    private VBox createMcpServerControls()
    {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));
        
        mcpStatusLabel = new Label();
        updateMcpStatusLabel();
        
        mcpToggleButton = new Button();
        updateMcpToggleButton();
        mcpToggleButton.setOnAction(e -> toggleMcpServer());
        
        HBox controlsBox = new HBox(15);
        controlsBox.getChildren().addAll(mcpToggleButton, mcpStatusLabel);
        
        box.getChildren().add(controlsBox);
        return box;
    }
    
    private void toggleMcpServer()
    {
        if (mcpController.isMcpServerRunning())
        {
            mcpController.stopMcpServer();
        }
        else
        {
            settingsController.setMcpServerPort(tmpMcpServerPort);
            mcpController.startMcpServer();
        }
        updateMcpStatusLabel();
        updateMcpToggleButton();
    }
    
    private void updateMcpStatusLabel()
    {
        if (mcpController.isMcpServerRunning())
        {
            mcpStatusLabel.setText("Server running on port " + mcpController.getMcpServerPort());
            mcpStatusLabel.setStyle("-fx-text-fill: green;");
        }
        else
        {
            mcpStatusLabel.setText("Server stopped");
            mcpStatusLabel.setStyle("-fx-text-fill: gray;");
        }
    }
    
    private void updateMcpToggleButton()
    {
        if (mcpController.isMcpServerRunning())
        {
            mcpToggleButton.setText("Stop Server");
        }
        else
        {
            mcpToggleButton.setText("Start Server");
        }
    }
}
