package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.FollowOrCreateButtonCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

public class FollowRefOrOpenColumn extends TableColumn<JsonNodeWithPath, String>
{
    public FollowRefOrOpenColumn(ReadableModel model, EditorWindowManager manager)
    {
        super();
        
        setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPath()));
        
        setCellFactory(param -> new FollowOrCreateButtonCell(model, manager, this));
    }
    
    
}
