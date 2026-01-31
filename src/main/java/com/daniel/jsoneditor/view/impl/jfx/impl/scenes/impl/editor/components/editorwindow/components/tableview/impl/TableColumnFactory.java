package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.EditorTableColumn;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.FollowRefOrOpenColumn;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.GitBlameColumn;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for creating and managing table columns.
 * Extracted from EditorTableViewImpl to follow Single Responsibility Principle.
 */
public class TableColumnFactory
{
    private final EditorWindowManager manager;
    private final Controller controller;
    private final ReadableModel model;
    private final JsonEditorEditorWindow window;
    
    public TableColumnFactory(EditorWindowManager manager, Controller controller,
                             ReadableModel model, JsonEditorEditorWindow window)
    {
        this.manager = manager;
        this.controller = controller;
        this.model = model;
        this.window = window;
    }
    
    /**
     * Creates all columns for the table based on properties and array status.
     *
     * @param properties the properties to create columns for
     * @param isArray whether this is an array view
     * @param parentTableView reference to the parent table view
     * @return list of created columns
     */
    public List<TableColumn<JsonNodeWithPath, String>> createColumns(
            List<Pair<Pair<String, Boolean>, JsonNode>> properties,
            boolean isArray,
            EditorTableViewImpl parentTableView)
    {
        final List<TableColumn<JsonNodeWithPath, String>> columns = new ArrayList<>(createPropertyColumns(properties, parentTableView));
        
        if (model.isGitBlameAvailable())
        {
            final GitBlameColumn blameColumn = new GitBlameColumn(model);
            blameColumn.setVisible(false);
            columns.add(blameColumn);
        }
        
        if (isArray)
        {
            columns.add(new FollowRefOrOpenColumn(model, manager));
            columns.add(createDeleteButtonColumn());
        }
        
        return columns;
    }
    
    private List<EditorTableColumn> createPropertyColumns(List<Pair<Pair<String, Boolean>, JsonNode>> properties,
                                                         EditorTableViewImpl parentTableView)
    {
        final List<EditorTableColumn> columns = new ArrayList<>();
        for (Pair<Pair<String, Boolean>, JsonNode> property : properties)
        {
            final String propertyName = property.getKey().getKey();
            final boolean isRequired = property.getKey().getValue();
            final JsonNode propertyNode = property.getValue();
            columns.add(new EditorTableColumn(manager, controller, model, window, parentTableView, propertyNode, propertyName, isRequired));
        }
        return columns;
    }
    
    private TableColumn<JsonNodeWithPath, String> createDeleteButtonColumn()
    {
        final TableColumn<JsonNodeWithPath, String> deleteColumn = new TableColumn<>("Delete");
        
        deleteColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPath()));
        
        deleteColumn.setCellFactory(param -> new TableCell<JsonNodeWithPath, String>()
        {
            @Override
            protected void updateItem(String path, boolean empty)
            {
                super.updateItem(path, empty);
                if (empty || path == null)
                {
                    setGraphic(null);
                }
                else
                {
                    setGraphic(createRemoveButton(path));
                }
            }
        });
        
        return deleteColumn;
    }
    
    private Button createRemoveButton(String path)
    {
        final Button removeButton = new Button();
        ButtonHelper.setButtonImage(removeButton, "/icons/material/darkmode/outline_delete_white_24dp.png");
        removeButton.setOnAction(event -> controller.removeNodes(List.of(path)));
        removeButton.setMaxHeight(Double.MAX_VALUE);
        return removeButton;
    }
}
