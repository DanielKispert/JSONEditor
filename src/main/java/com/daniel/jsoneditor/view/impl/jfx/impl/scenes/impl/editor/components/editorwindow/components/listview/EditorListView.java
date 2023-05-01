package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.scene.layout.GridPane;

public abstract class EditorListView extends GridPane
{
    public abstract void setSelection(JsonNodeWithPath selection);
}
