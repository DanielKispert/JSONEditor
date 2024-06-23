package com.daniel.jsoneditor.view.impl.jfx.buttons;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.JsonEditorNavbar;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class NavBarSwitchButton extends Button
{
    
    private final ReadableModel model;
    
    private JsonEditorNavbar navbar;
    
    private boolean showsGraph = false;
    
    public NavBarSwitchButton(ReadableModel model, JsonEditorNavbar navbar)
    {
        super();
        this.model = model;
        this.navbar = navbar;
        ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_graph_white_24dp.png");
        setOnAction(actionEvent -> handleClick());
        setTooltip(new Tooltip("Show as graph"));
    }
    
    public void setSelection(JsonNodeWithPath selection)
    {
        String path = selection.getPath();
        boolean visible = model.getReferenceableObject(path) != null && !selection.isArray(); //TODO
        this.setVisible(visible);
        this.setManaged(visible);
    }
    
    private void handleClick()
    {
        if (showsGraph)
        {
            navbar.showNavTreeView();
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_graph_white_24dp.png");
            setTooltip(new Tooltip("Show as graph"));
        }
        else
        {
            navbar.showGraphView();
            ButtonHelper.setButtonImage(this, "/icons/material/darkmode/outline_menu_white_24dp.png");
            setTooltip(new Tooltip("Show as tree"));
        }
        showsGraph = !showsGraph;
    }
}
