package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.EditorListView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.JsonEditorListCell;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;

import java.util.ArrayList;
import java.util.List;

public class EditorListViewImpl extends EditorListView
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private JsonNodeWithPath selection;
    
    public EditorListViewImpl(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.manager = manager;
        this.model = model;
        setCellFactory(jsonNodeWithPathListView -> new JsonEditorListCell(this, model, controller));
        VBox.setVgrow(this, Priority.ALWAYS);
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        this.selection = nodeWithPath;
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath());
        List<JsonNodeWithPath> childNodes = new ArrayList<>(); //either a list of array items or object fields
        if (node.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                childNodes.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (nodeWithPath.isObject())
        {
            childNodes = NodeSearcher.getAllChildNodesFromSchema(nodeWithPath, schema);
        }
        getItems().setAll(childNodes);
    }
    
    public JsonNodeWithPath getSelection()
    {
        return selection;
    }
    
    public EditorWindowManager getManager()
    {
        return manager;
    }
    
    
    public static VBox makeFieldWithTitle(String title, String value)
    {
        VBox fieldBox = new VBox();
        Label fieldTitle = new Label(title);
        fieldTitle.setTextFill(Color.GREY);
        fieldTitle.setFont(Font.font(null, FontWeight.NORMAL, 12));
        Label fieldValue = new Label(value);
        fieldValue.setTextFill(Color.BLACK);
        fieldValue.setFont(Font.font(null, FontWeight.NORMAL, 16));
        fieldBox.getChildren().addAll(fieldTitle, fieldValue);
        HBox.setHgrow(fieldTitle, Priority.ALWAYS);
        HBox.setHgrow(fieldValue, Priority.ALWAYS);
        HBox.setHgrow(fieldBox, Priority.ALWAYS);
        return fieldBox;
    }
}
