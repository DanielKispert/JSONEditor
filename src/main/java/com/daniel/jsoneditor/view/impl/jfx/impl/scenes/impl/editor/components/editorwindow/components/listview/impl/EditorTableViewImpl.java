package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.impl;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.EditorTableView;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.JsonEditorListCell;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;

import java.util.ArrayList;
import java.util.List;


/**
 * shows a list of child objects of a selection. If the selection is an array, it shows a list of its items. If the selection is an
 * object, it shows the child nodes of the object.
 * This view consists of two gridpanes, one contains
 */
public class EditorTableViewImpl extends EditorTableView
{
    private final ReadableModel model;
    
    private final EditorWindowManager manager;
    
    private JsonNodeWithPath selection;
    
    public EditorTableViewImpl(EditorWindowManager manager, ReadableModel model, Controller controller)
    {
        this.manager = manager;
        this.model = model;
        //setCellFactory(jsonNodeWithPathListView -> new JsonEditorListCell(this, model, controller));
        VBox.setVgrow(this, Priority.ALWAYS);
    }
    
    public void setSelection(JsonNodeWithPath nodeWithPath)
    {
        this.selection = nodeWithPath;
        //
        JsonNode node = nodeWithPath.getNode();
        JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath());
        ObservableList<JsonNodeWithPath> childNodes = FXCollections.observableArrayList(); //either a list of array items or object fields
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
            childNodes = FXCollections.observableArrayList(NodeSearcher.getAllChildNodesFromSchema(nodeWithPath, schema));
        }
        setItems(childNodes);
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
