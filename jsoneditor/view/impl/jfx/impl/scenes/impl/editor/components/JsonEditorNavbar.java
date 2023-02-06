package jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.model.json.JsonNodeWithPath;

import java.util.Iterator;
import java.util.Map;

public class JsonEditorNavbar extends TreeView<JsonNodeWithPath>
{
    private final ReadableModel model;
    
    private final Controller controller;
    
    private TreeItem<JsonNodeWithPath> selectedItem;
    
    public JsonEditorNavbar(ReadableModel model, Controller controller)
    {
        
        this.model = model;
        this.controller = controller;
        this.selectedItem = null;
        SplitPane.setResizableWithParent(this, false);
        setRoot(makeTree(model.getRootJson()));
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleNavbarClick(newValue));
        
    }
    
    private TreeItem<JsonNodeWithPath> makeTree(JsonNode node)
    {
        TreeItem<JsonNodeWithPath> root = new TreeItem<>(new JsonNodeWithPath(node, ""));
        root.setExpanded(true);
        populateItem(root);
        return root;
    }
    
    private void populateItem(TreeItem<JsonNodeWithPath> item)
    {
        JsonNode node = item.getValue().getNode();
        if (node.getNodeType().equals(JsonNodeType.OBJECT))
        {
            populateForObject(item);
        }
        else if (JsonNodeType.ARRAY.equals(node.getNodeType()))
        {
            populateForArray(item);
        }
    }
    
    private void populateForObject(TreeItem<JsonNodeWithPath> parent)
    {
        Iterator<Map.Entry<String, JsonNode>> fields = parent.getValue().getNode().fields();
        String pathForFields = parent.getValue().getPath() + "/";
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (value.getNodeType() == JsonNodeType.OBJECT || value.getNodeType() == JsonNodeType.ARRAY)
            {
                String pathForNode = pathForFields + field.getKey();
                TreeItem<JsonNodeWithPath> child = new TreeItem<>(new JsonNodeWithPath(value, pathForNode));
                populateItem(child);
                parent.getChildren().add(child);
            }
        }
    }
    
    
    private void populateForArray(TreeItem<JsonNodeWithPath> parent)
    {
        String pathForItems = parent.getValue().getPath() + "/";
        int index = 0;
        for (JsonNode item : parent.getValue().getNode())
        {
            if (item.getNodeType() == JsonNodeType.OBJECT || item.getNodeType() == JsonNodeType.ARRAY)
            {
                String pathForNode = pathForItems + index++;
                TreeItem<JsonNodeWithPath> child = new TreeItem<>(new JsonNodeWithPath(item, pathForNode));
                populateItem(child);
                parent.getChildren().add(child);
            }
        }
    }
    
    private void handleNavbarClick(TreeItem<JsonNodeWithPath> item)
    {
        selectedItem = item;
        controller.chooseNodeFromNavbar(item.getValue());
    }
    
    public void updateTreeAndSelectParent()
    {
        TreeItem<JsonNodeWithPath> parent = selectedItem.getParent();
        parent.getChildren().remove(selectedItem);
        selectedItem = parent;
        getSelectionModel().select(selectedItem);
    }
}
