package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow;

import javafx.scene.layout.HBox;

public interface EditorWindowManager
{
    HBox getEditorWindows();
    
    /**
     * a path gets selected from the navbar and has to be displayed in an editor window (we'll decide which one)
     */
    void selectFromNavbar(String path);
    
    void updateEditors();
}
