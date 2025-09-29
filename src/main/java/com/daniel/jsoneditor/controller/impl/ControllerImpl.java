package com.daniel.jsoneditor.controller.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.json.JsonFileReaderAndWriter;
import com.daniel.jsoneditor.controller.impl.json.VariableHelper;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.controller.impl.json.impl.JsonNodeMerger;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.controller.settings.impl.SettingsControllerImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.daniel.jsoneditor.model.commands.CommandFactory;
import com.daniel.jsoneditor.model.commands.impl.AddNodeToArrayCommand;
import com.daniel.jsoneditor.model.commands.impl.MoveItemCommand;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.model.observe.Subject;
import com.daniel.jsoneditor.model.settings.Settings;
import com.daniel.jsoneditor.model.statemachine.impl.Event;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.daniel.jsoneditor.view.View;
import com.daniel.jsoneditor.view.impl.ViewImpl;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.VariableReplacementDialog;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;


public class ControllerImpl implements Controller, Observer
{
    
    private final WritableModel model;
    
    private final ReadableModel readableModel;
    
    private final View view;
    
    private final List<Subject> subjects;
    
    private final SettingsController settingsController;
    
    private final CommandManager commandManager;
    
    private final CommandFactory commandFactory;
    
    public ControllerImpl(WritableModel model, ReadableModel readableModel, Stage stage)
    {
        this.settingsController = new SettingsControllerImpl();
        this.commandManager = new CommandManager(model); // braucht model für Undo/Redo
        this.commandFactory = readableModel.getCommandFactory();
        this.model = model;
        this.readableModel = readableModel;
        this.subjects = new ArrayList<>();
        this.view = new ViewImpl(readableModel, this, stage);
        this.view.observe(this.readableModel.getForObservation());
    }
    
    @Override
    public SettingsController getSettingsController()
    {
        return settingsController;
    }
    
    
    @Override
    public void update()
    {
    }
    
    @Override
    public void observe(Subject subjectToObserve)
    {
        subjects.add(subjectToObserve);
        subjectToObserve.registerObserver(this);
        
    }
    
    @Override
    public void launchFinished()
    {
        model.sendEvent(new Event(EventEnum.READ_JSON_AND_SCHEMA));
    }
    
    @Override
    public void jsonAndSchemaSelected(File jsonFile, File schemaFile, File settingsFile)
    {
        if (jsonFile != null && schemaFile != null)
        {
            // grab json from files and validate
            JsonFileReaderAndWriter reader = new JsonFileReaderAndWriterImpl();
            JsonNode json = reader.getJsonFromFile(jsonFile);
            JsonSchema schema = reader.getSchemaFromFileResolvingRefs(schemaFile);
            handleJsonValidation(json, schema, () -> {
                // settings file is optional
                if (settingsFile != null)
                {
                    Settings settingsFromFile = reader.getJsonFromFile(settingsFile, Settings.class, true);
                    if (settingsFromFile != null)
                    {
                        model.setSettings(settingsFromFile);
                    }
                }
                model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
            });
            
        }
        else
        {
            view.selectJsonAndSchema();
        }
        
    }
    
    @Override
    public void moveItemToIndex(JsonNodeWithPath newParent, JsonNodeWithPath item, int index)
    {
        if (item == null || item.getPath() == null)
        {
            return;
        }
        // cross-parent moves not implemented (only reorder inside same array)
        commandManager.executeCommand(commandFactory.moveItemCommand(item.getPath(), index));
    }
    
    @Override
    public String resolveVariablesInJson(String text)
    {
        Set<String> variables = VariableHelper.findVariables(text);
        
        if (!variables.isEmpty())
        {
            VariableReplacementDialog dialog = new VariableReplacementDialog(variables);
            Map<String, String> replacements = dialog.showAndWait().orElse(null);
            
            if (replacements != null)
            {
                return VariableHelper.replaceVariables(text, replacements);
            }
        }
        return text;
    }
    
    @Override
    public void importAtNode(String path, String content)
    {
        JsonFileReaderAndWriter jsonReader = new JsonFileReaderAndWriterImpl();
        JsonNodeWithPath existingNodeAtPath = readableModel.getNodeForPath(path == null ? "" : path);
        JsonNode contentNode = jsonReader.getNodeFromString(content);
        JsonNode mergedNode = JsonNodeMerger.createMergedNode(readableModel, existingNodeAtPath, contentNode);
        JsonSchema schemaAtPath = readableModel.getSubschemaForPath(path);
        if (mergedNode != null && SchemaHelper.validateJsonWithSchema(mergedNode, schemaAtPath))
        {
            // the node exists and is valid for its current location
            commandManager.executeCommand(commandFactory.setNodeCommand(path, mergedNode));
        }
    }
    
    @Override
    public void exportNode(String path)
    {
        // exporting a node does not require writing to the model, hence we only need the controller and the readable model
        JsonNodeWithPath nodeWithPath = readableModel.getNodeForPath(path);
        if (nodeWithPath != null)
        {
            String fileWithEnding = readableModel.getCurrentJSONFile().getName();
            int lastDotIndex = fileWithEnding.lastIndexOf(".");
            String fileWithoutEnding = (lastDotIndex != -1) ? fileWithEnding.substring(0, lastDotIndex) : fileWithEnding;
            String filename = fileWithoutEnding + "_export" + nodeWithPath.getPath().replace("/", "_") + ".json";
            exportJsonNode(filename, nodeWithPath.getNode());
        }
    }
    
    @Override
    public void exportNodeWithDependencies(String path)
    {
        JsonNodeWithPath nodeWithPath = readableModel.getNodeForPath(path);
        if (nodeWithPath != null)
        {
            // we want to export the node and all parent nodes of the node (but no other child nodes of the parent nodes)
            List<String> pathsToExport = readableModel.getDependentPaths(nodeWithPath);
            String selectedNode = readableModel.getNodeForPath(path).getPath();
            if (!pathsToExport.contains(selectedNode))
            {
                pathsToExport.add(selectedNode);
            }
            
            String fileWithEnding = readableModel.getCurrentJSONFile().getName();
            int lastDotIndex = fileWithEnding.lastIndexOf(".");
            String fileWithoutEnding = (lastDotIndex != -1) ? fileWithEnding.substring(0, lastDotIndex) : fileWithEnding;
            String filename = fileWithoutEnding + "_export_with_dependencies" + nodeWithPath.getPath().replace("/", "_") + ".json";
            exportJsonNode(filename, readableModel.getExportStructureForNodes(pathsToExport));
        }
    }
    
    private void exportJsonNode(String exportFilename, JsonNode node)
    {
        File directory = readableModel.getCurrentJSONFile().getParentFile();
        File exportFile = new File(directory, exportFilename);
        JsonFileReaderAndWriter writer = new JsonFileReaderAndWriterImpl();
        writer.writeJsonToFile(node, exportFile);
        model.sendEvent(new Event(EventEnum.EXPORT_SUCCESSFUL));
        
    }
    
    @Override
    public void removeNodes(List<String> paths)
    {
        if (paths == null || paths.isEmpty())
        {
            return;
        }
        commandManager.executeCommand(commandFactory.removeNodesCommand(paths));
    }
    
    @Override
    public void removeNode(String path)
    {
        if (path == null)
        {
            return;
        }
        commandManager.executeCommand(commandFactory.removeNodeCommand(path));
    }
    
    @Override
    public void addNewNodeToArray(String path)
    {
        commandManager.executeCommand(commandFactory.addNodeToArrayCommand(path));
    }
    
    @Override
    public void createNewReferenceableObjectNodeWithKey(String pathOfReferenceableObject, String key)
    {
        if (pathOfReferenceableObject == null || key == null)
        {
            return;
        }
        commandManager.executeCommand(commandFactory.createReferenceableObjectCommand(pathOfReferenceableObject, key));
        
    }
    
    @Override
    public void sortArray(String path)
    {
        if (path == null)
        {
            return;
        }
        commandManager.executeCommand(commandFactory.sortArrayCommand(path));
    }
    
    @Override
    public void duplicateArrayNode(String path)
    {
        if (path == null)
        {
            return;
        }
        commandManager.executeCommand(commandFactory.duplicateArrayItemCommand(path));
    }
    
    @Override
    public void duplicateReferenceableObjectForLinking(String referencePath, String pathToDuplicate)
    {
        if (referencePath == null || pathToDuplicate == null)
        {
            return;
        }
        commandManager.executeCommand(commandFactory.duplicateReferenceAndLinkCommand(referencePath, pathToDuplicate));
    }
    
    @Override
    public void saveToFile()
    {
        JsonFileReaderAndWriter jsonWriter = new JsonFileReaderAndWriterImpl();
        jsonWriter.writeJsonToFile(readableModel.getRootJson(), readableModel.getCurrentJSONFile());
        model.sendEvent(new Event(EventEnum.SAVING_SUCCESSFUL));
    }
    
    @Override
    public void refreshFromFile()
    {
        // TODO we can add a dialog to ask the user if they're really sure here
        JsonFileReaderAndWriter reader = new JsonFileReaderAndWriterImpl();
        JsonNode json = reader.getJsonFromFile(readableModel.getCurrentJSONFile());
        handleJsonValidation(json, readableModel.getRootSchema(), () -> model.refreshJsonNode(json));
    }
    
    @Override
    public String searchForNode(String path, String value)
    {
        return readableModel.searchForNode(path, value);
    }
    
    private void handleJsonValidation(JsonNode json, JsonSchema schema, Runnable onSuccess)
    {
        if (SchemaHelper.validateJsonWithSchema(json, schema))
        {
            onSuccess.run();
        }
        else
        {
            view.cantValidateJson();
        }
    }
    
    @Override
    public void openNewJson()
    {
        launchFinished();
    }
    
    @Override
    public void generateJson()
    {
    
    }
    
    @Override
    public void setValueAtPath(String path, Object value)
    {
        String parentPath = PathHelper.getParentPath(path);
        String propertyName = PathHelper.getLastPathSegment(path);
        commandManager.executeCommand(commandFactory.setValueAtNodeCommand(parentPath, propertyName, value));
    }
    
    @Override
    public void copyToClipboard(String path)
    {
        JsonNodeWithPath item = readableModel.getNodeForPath(path);
        if (item != null)
        {
            ClipboardContent content = new ClipboardContent();
            content.putString(item.getNode().toString());
            Clipboard.getSystemClipboard().setContent(content);
            view.showToast(Toasts.COPIED_TO_CLIPBOARD_TOAST);
        }
        else
        {
            view.showToast(Toasts.ERROR_TOAST);
            System.out.println("Failed to copy to clipboard, " + path + " is not a valid path");
        }
    }
    
    @Override
    public void pasteFromClipboardReplacingChild(String pathToInsert)
    {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString())
        {
            String jsonString = clipboard.getString();
            try
            {
                JsonNode jsonNode = new JsonFileReaderAndWriterImpl().getNodeFromString(jsonString);
                JsonNodeWithPath itemToInsertAt = readableModel.getNodeForPath(pathToInsert);
                if (itemToInsertAt == null)
                {
                    view.showToast(Toasts.ERROR_TOAST);
                    return;
                }
                if (SchemaHelper.validateJsonWithSchema(jsonNode, readableModel.getSubschemaForPath(pathToInsert)))
                {
                    commandManager.executeCommand(commandFactory.setNodeCommand(pathToInsert, jsonNode));
                    view.showToast(Toasts.PASTED_FROM_CLIPBOARD_TOAST);
                    
                }
                else if (itemToInsertAt.isArray() && SchemaHelper.validateJsonWithSchema(jsonNode,
                        readableModel.getSubschemaForPath(pathToInsert + "/0")))
                {
                    commandManager.executeCommand(commandFactory.setNodeCommand(itemToInsertAt.getPath() + "/" + itemToInsertAt.getNode().size(), jsonNode));
                    view.showToast(Toasts.PASTED_FROM_CLIPBOARD_TOAST);
                }
                else
                {
                    view.showToast(Toasts.ERROR_TOAST);
                }
            }
            catch (Exception e)
            {
                view.showToast(Toasts.ERROR_TOAST);
            }
        }
        
    }
    
    @Override
    public void pasteFromClipboardIntoParent(String parentPath)
    {
        JsonNodeWithPath parentNode = readableModel.getNodeForPath(parentPath);
        if (parentNode == null || !parentNode.isArray())
        {
            view.showToast(Toasts.ERROR_TOAST);
            return;
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString())
        {
            String jsonString = clipboard.getString();
            try
            {
                JsonNode jsonNode = new JsonFileReaderAndWriterImpl().getNodeFromString(jsonString);
                if (SchemaHelper.validateJsonWithSchema(jsonNode, readableModel.getSubschemaForPath(parentPath)))
                {
                    int arraySize = parentNode.getNode().size();
                    commandManager.executeCommand(commandFactory.setNodeCommand(parentNode.getPath() + "/" + arraySize, jsonNode));
                    view.showToast(Toasts.PASTED_FROM_CLIPBOARD_TOAST);
                }
                else
                {
                    view.showToast(Toasts.ERROR_TOAST);
                }
            }
            catch (Exception e)
            {
                view.showToast(Toasts.ERROR_TOAST);
            }
        }
        
    }
    
    @Override
    public void undoLastAction()
    {
        commandManager.undo();
    }
    
    @Override
    public void redoLastAction()
    {
        commandManager.redo();
    }
}
