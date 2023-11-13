package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.menubar;

import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.toolbar.FindDialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;


public class JsonEditorMenuBar extends MenuBar
{
    public JsonEditorMenuBar(ReadableModel model, Controller controller, EditorWindowManager manager)
    {
        super();
        setUseSystemMenuBar(true);
        
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> controller.openNewJson());
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(event -> controller.saveToFile());
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setOnAction(event -> controller.refreshFromFile());
        refreshItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        fileMenu.getItems().addAll(openItem, saveItem, refreshItem);
    
        Menu inspectMenu = new Menu("Inspect");
        MenuItem findItem = new MenuItem("Find anything");
        findItem.setOnAction(event -> {
            FindDialog dialog = new FindDialog(model.getReferenceableObjectKeys());
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                String foundNode = controller.searchForReferenceableObject(s);
                if (foundNode != null)
                {
                    if (manager.canAnotherWindowBeAdded())
                    {
                        manager.selectInNewWindow(foundNode);
                    }
                    else
                    {
                        manager.selectFromNavbar(foundNode);
                    }
                }
            });
        });
        findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        inspectMenu.getItems().add(findItem);
        
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> new AboutDialog().showAndWait());
        helpMenu.getItems().add(aboutItem);
        getMenus().addAll(fileMenu, inspectMenu, helpMenu);
    }
}
