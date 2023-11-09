package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.menubar;

import com.daniel.jsoneditor.controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;


public class JsonEditorMenuBar extends MenuBar
{
    public JsonEditorMenuBar(Controller controller)
    {
        super();
        setUseSystemMenuBar(true);
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> controller.openNewJson());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(event -> controller.saveToFile());
        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setOnAction(event -> controller.refreshFromFile());
        fileMenu.getItems().addAll(openItem, saveItem, refreshItem);
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        helpMenu.getItems().add(aboutItem);
        getMenus().addAll(fileMenu, helpMenu);
    }
}
