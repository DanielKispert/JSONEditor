package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.listview.field;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.Node;
import javafx.scene.control.TextField;

public class TextNodeField extends EditorField
{
    public TextNodeField(ObjectNode parent, String title)
    {
        super(parent, title);
    }
    
    
    protected void onTextChange(String newValue)
    {
        parent.put(key, newValue);
    }
    
    @Override
    protected Node getInputField()
    {
        TextField fieldInput = new TextField(parent.get(key).asText());
        fieldInput.textProperty().addListener((observableValue, s, t1) -> onTextChange(t1));
        return fieldInput;
    }
}
