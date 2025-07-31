package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.menubar;

import java.util.Optional;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.AboutDialog;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.SettingsDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.FindDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;


public class JsonEditorMenuBar extends MenuBar
{
    public JsonEditorMenuBar(ReadableModel model, Controller controller, EditorWindowManager manager, JsonEditorNavbar navbar)
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
        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setOnAction(event -> new SettingsDialog(controller.getSettingsController()).showAndWait());
        fileMenu.getItems().addAll(openItem, saveItem, refreshItem, settingsItem);
        
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setOnAction(event -> controller.undoLastAction());
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setOnAction(event -> controller.redoLastAction());
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));
        editMenu.getItems().addAll(undoItem, redoItem);
        
        Menu inspectMenu = new Menu("Inspect");
        MenuItem findItem = makeFindAnythingItem(model, manager, navbar);
        inspectMenu.getItems().add(findItem);
        
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> new AboutDialog().showAndWait());
        helpMenu.getItems().add(aboutItem);
        getMenus().addAll(fileMenu, editMenu, inspectMenu, helpMenu);
    }
    
    private static MenuItem makeFindAnythingItem(ReadableModel model, EditorWindowManager manager, JsonEditorNavbar navbar)
    {
        MenuItem findItem = new MenuItem("Find anything");
        findItem.setOnAction(event -> {
            FindDialog dialog = new FindDialog(model.getReferenceableObjectInstances());
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                navbar.selectPath(s);
                manager.openPath(s);
            });
        });
        findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        return findItem;
    }
}
