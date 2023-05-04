package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.control.TableView;


public abstract class EditorTableView extends TableView<JsonNodeWithPath>
{
    public abstract void setSelection(JsonNodeWithPath selection);
}
