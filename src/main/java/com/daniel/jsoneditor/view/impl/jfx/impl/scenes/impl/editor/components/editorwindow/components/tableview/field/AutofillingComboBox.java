package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.field;

import java.util.List;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableColumn;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.TextTableCell;
import javafx.scene.control.ComboBox;


/**
 * UI element that is an autofilling combobox
 */
public class AutofillingComboBox extends ComboBox<String>
{
    private final TextTableCell parentCell;
    
    private final ReadableModel model;
    
    public AutofillingComboBox(TextTableCell parentCell, ReadableModel model, String initialValue)
    {
        this.parentCell = parentCell;
        this.model = model;
        setValue(initialValue);
        setEditable(userEnteredValuesAllowed);
        getItems().setAll(suggestions);
    }
    

}
