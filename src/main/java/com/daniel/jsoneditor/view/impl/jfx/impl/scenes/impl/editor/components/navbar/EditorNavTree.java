package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.paths.PathHelper;
import com.daniel.jsoneditor.view.impl.jfx.dialogs.ImportDialog;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;


public class EditorNavTree extends TreeView<JsonNodeWithPath> implements NavbarElement
{
    private static final int LARGE_ARRAY_THRESHOLD = 20;
    private static final String PLACEHOLDER_STYLE_CLASS = "navbar-placeholder-cell";
    
    private final ReadableModel model;
    private final EditorWindowManager editorWindowManager;
    private final Controller controller;
    private final Stage stage;
    private final JsonEditorNavbar navbar;
    private String selectedPath;
    
    private final Set<String> fullyExpandedArrays = new HashSet<>();
    private final Set<String> visitedItemPaths = new HashSet<>();
    
    public EditorNavTree(JsonEditorNavbar navbar, ReadableModel model, Controller controller, EditorWindowManager editorWindowManager,
            Stage stage)
    {
        this.model = model;
        this.controller = controller;
        this.editorWindowManager = editorWindowManager;
        this.stage = stage;
        this.navbar = navbar;
        SplitPane.setResizableWithParent(this, false);
        setRoot(makeTree());
        setContextMenu(makeContextMenu());
        setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
            {
                handleSelectedItemClick();
            }
        });
        setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER)
            {
                handleSelectedItemClick();
            }
        });
        setCellFactory(tv -> new NavbarTreeCell());
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }
    
    private void handleSelectedItemClick()
    {
        final TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null)
        {
            handleNavbarClick(selectedItem);
        }
    }
    
    private static class NavbarTreeCell extends TreeCell<JsonNodeWithPath>
    {
        private Tooltip tooltip;
        
        NavbarTreeCell()
        {
            hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                final JsonNodeWithPath item = getItem();
                if (isNowHovered && item != null && !(getTreeItem() instanceof NavbarPlaceholderItem))
                {
                    if (tooltip == null || !tooltip.getText().equals(item.getDisplayName()))
                    {
                        tooltip = TooltipHelper.makeTooltipFromJsonNode(item.getNode());
                        setTooltip(tooltip);
                    }
                }
            });
        }
        
        @Override
        protected void updateItem(JsonNodeWithPath item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty || item == null)
            {
                setText(null);
                setTooltip(null);
                setTranslateX(0);
                getStyleClass().remove(PLACEHOLDER_STYLE_CLASS);
            }
            else if (getTreeItem() instanceof NavbarPlaceholderItem)
            {
                setText(((NavbarPlaceholderItem) getTreeItem()).getDisplayText());
                setTooltip(null);
                // shift one indent level to the left so it aligns with its siblings' parent
                setTranslateX(-10);
                if (!getStyleClass().contains(PLACEHOLDER_STYLE_CLASS))
                {
                    getStyleClass().add(PLACEHOLDER_STYLE_CLASS);
                }
            }
            else
            {
                setText(item.toString());
                setTranslateX(0);
                getStyleClass().remove(PLACEHOLDER_STYLE_CLASS);
            }
        }
    }
    
    private void withSelectedPath(Consumer<String> action)
    {
        final TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
        if (selectedItem != null)
        {
            action.accept(selectedItem.getValue().getPath());
        }
    }
    
    private void handleKeyPressed(KeyEvent event)
    {
        if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(event))
        {
            withSelectedPath(controller::copyToClipboard);
        }
        else if (new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN).match(event))
        {
            withSelectedPath(controller::pasteFromClipboardReplacingChild);
        }
    }
    
    private ContextMenu makeContextMenu()
    {
        final ContextMenu contextMenu = new ContextMenu();
        
        final MenuItem addItemItem = new MenuItem("Add Item");
        final MenuItem duplicateItem = new MenuItem("Duplicate Item");
        final MenuItem newWindowItem = new MenuItem("Open in new Window");
        final MenuItem copyItem = new MenuItem("Copy");
        final MenuItem pasteItem = new MenuItem("Paste");
        final MenuItem deleteItem = new MenuItem("Delete");
        final MenuItem sortArray = new MenuItem("Sort");
        final MenuItem importItem = new MenuItem("Import");
        final MenuItem exportItem = new MenuItem("Export");
        final MenuItem exportWithDependenciesItem = new MenuItem("Export with Dependencies");
        
        copyItem.setOnAction(e -> withSelectedPath(controller::copyToClipboard));
        pasteItem.setOnAction(e -> withSelectedPath(controller::pasteFromClipboardReplacingChild));
        addItemItem.setOnAction(e -> withSelectedPath(controller::addNewNodeToArray));
        sortArray.setOnAction(e -> withSelectedPath(controller::sortArray));
        deleteItem.setOnAction(e -> withSelectedPath(p -> controller.removeNodes(List.of(p))));
        exportItem.setOnAction(e -> withSelectedPath(controller::exportNode));
        exportWithDependenciesItem.setOnAction(e -> withSelectedPath(controller::exportNodeWithDependencies));
        newWindowItem.setOnAction(e -> withSelectedPath(p -> editorWindowManager.openInNewWindowIfPossible(p, false)));
        duplicateItem.setOnAction(e -> {
            final TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null && selectedItem.getParent().getValue().isArray())
            {
                controller.duplicateArrayNode(selectedItem.getValue().getPath());
            }
        });
        importItem.setOnAction(e -> withSelectedPath(path -> {
            new ImportDialog(stage).showAndWait().ifPresent(json -> {
                controller.importAtNode(path, controller.resolveVariablesInJson(json));
            });
        }));
        
        contextMenu.getItems().addAll(newWindowItem, addItemItem, copyItem, pasteItem, duplicateItem, sortArray, importItem, exportItem,
                exportWithDependenciesItem, deleteItem);
        
        setOnContextMenuRequested(event -> {
            final TreeItem<JsonNodeWithPath> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null)
            {
                if (selectedItem.getParent() != null)
                {
                    duplicateItem.setVisible(selectedItem.getParent().getValue().isArray());
                }
                final boolean isArray = selectedItem.getValue().isArray();
                addItemItem.setVisible(isArray);
                sortArray.setVisible(isArray);
            }
            else
            {
                duplicateItem.setVisible(false);
            }
            newWindowItem.setVisible(editorWindowManager.canAnotherWindowBeAdded());
        });
        
        return contextMenu;
    }
    
    private NavbarItem makeTree()
    {
        model.getRootJson();
        final NavbarItem root = new NavbarItem(model, "");
        root.setExpanded(true);
        populateItem(root);
        return root;
    }
    
    private void populateItem(NavbarItem item)
    {
        final JsonNode node = item.getValue().getNode();
        if (node.isObject())
        {
            populateForObject(item);
        }
        else if (node.isArray())
        {
            populateForArray(item);
        }
    }
    
    private void populateForObject(NavbarItem parent)
    {
        final Iterator<Map.Entry<String, JsonNode>> fields = parent.getValue().getNode().fields();
        final String pathPrefix = parent.getValue().getPath() + "/";
        while (fields.hasNext())
        {
            final Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isContainerNode())
            {
                final NavbarItem child = new NavbarItem(model, pathPrefix + field.getKey());
                populateItem(child);
                parent.getChildren().add(child);
            }
        }
    }
    
    private void populateForArray(NavbarItem parent)
    {
        final String arrayPath = parent.getValue().getPath();
        final JsonNode arrayNode = parent.getValue().getNode();
        final boolean showAll = arrayNode.size() <= LARGE_ARRAY_THRESHOLD || fullyExpandedArrays.contains(arrayPath);
        final String pathPrefix = arrayPath + "/";
        int index = 0;
        int hiddenCount = 0;
        for (final JsonNode item : arrayNode)
        {
            if (item.isContainerNode())
            {
                final String pathForNode = pathPrefix + index;
                if (showAll || visitedItemPaths.contains(pathForNode) || pathForNode.equals(selectedPath))
                {
                    final NavbarItem child = new NavbarItem(model, pathForNode);
                    populateItem(child);
                    parent.getChildren().add(child);
                }
                else
                {
                    hiddenCount++;
                }
            }
            index++;
        }
        if (hiddenCount > 0)
        {
            parent.getChildren().add(new NavbarPlaceholderItem(hiddenCount, arrayPath));
        }
    }
    
    private void handleNavbarClick(TreeItem<JsonNodeWithPath> item)
    {
        if (item instanceof NavbarPlaceholderItem)
        {
            final NavbarPlaceholderItem placeholder = (NavbarPlaceholderItem) item;
            fullyExpandedArrays.add(placeholder.getParentArrayPath());
            repopulateChildren(placeholder.getParentArrayPath());
            return;
        }
        final String path = item.getValue().getPath();
        this.selectedPath = path;
        visitedItemPaths.add(path);
        editorWindowManager.openPath(path, false);
        navbar.selectPath(path);
    }
    
    private void repopulateChildren(String path)
    {
        final TreeItem<JsonNodeWithPath> treeItem = findNavbarItem(getRoot(), path);
        if (treeItem != null)
        {
            final boolean wasExpanded = treeItem.isExpanded();
            treeItem.getChildren().clear();
            populateItem((NavbarItem) treeItem);
            treeItem.setExpanded(wasExpanded);
        }
    }
    
    @Override
    public void updateSingleElement(String path)
    {
        final TreeItem<JsonNodeWithPath> itemToUpdate = findNavbarItem(getRoot(), path);
        if (itemToUpdate != null)
        {
            itemToUpdate.setValue(model.getNodeForPath(path));
        }
        else
        {
            final TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(getRoot(), PathHelper.getParentPath(path));
            if (parentItem != null)
            {
                final NavbarItem newItem = new NavbarItem(model, path);
                populateItem(newItem);
                parentItem.getChildren().add(newItem);
            }
        }
    }
    
    private TreeItem<JsonNodeWithPath> findNavbarItem(TreeItem<JsonNodeWithPath> currentItem, String path)
    {
        if (currentItem.getValue().getPath().equals(path))
        {
            return currentItem;
        }
        final String currentPath = currentItem.getValue().getPath();
        if (!path.startsWith(currentPath.isEmpty() ? "" : currentPath + "/"))
        {
            return null;
        }
        for (final TreeItem<JsonNodeWithPath> childItem : currentItem.getChildren())
        {
            final TreeItem<JsonNodeWithPath> found = findNavbarItem(childItem, path);
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }
    
    @Override
    public void selectPath(String path)
    {
        if (!Objects.equals(selectedPath, path))
        {
            selectedPath = path;
            if (path != null && visitedItemPaths.add(path))
            {
                ensureVisibleInLargeArray(path);
            }
            selectNodeByPath(getRoot(), path);
        }
    }
    
    private void ensureVisibleInLargeArray(String path)
    {
        final String parentPath = PathHelper.getParentPath(path);
        if (parentPath == null)
        {
            return;
        }
        final JsonNodeWithPath parentNode = model.getNodeForPath(parentPath);
        if (parentNode != null && parentNode.isArray() && parentNode.getNode().size() > LARGE_ARRAY_THRESHOLD
                && !fullyExpandedArrays.contains(parentPath))
        {
            repopulateChildren(parentPath);
        }
    }
    
    private void selectNodeByPath(TreeItem<JsonNodeWithPath> node, String path)
    {
        if (node == null || node.getValue() == null)
        {
            return;
        }
        final String nodePath = node.getValue().getPath();
        if (nodePath.equals(path))
        {
            getSelectionModel().select(node);
            scrollTo(getRow(node));
            return;
        }
        if (!path.startsWith(nodePath.isEmpty() ? "" : nodePath + "/"))
        {
            return;
        }
        for (final TreeItem<JsonNodeWithPath> child : node.getChildren())
        {
            selectNodeByPath(child, path);
        }
    }
    
    @Override
    public void updateView()
    {
        setRoot(makeTree());
    }
    
    @Override
    public void handlePathAdded(String path)
    {
        visitedItemPaths.add(path);
        final TreeItem<JsonNodeWithPath> parentItem = findNavbarItem(getRoot(), PathHelper.getParentPath(path));
        if (parentItem != null)
        {
            final JsonNodeWithPath newNode = model.getNodeForPath(path);
            if (newNode != null && newNode.getNode().isContainerNode())
            {
                final NavbarItem newItem = new NavbarItem(model, path);
                populateItem(newItem);
                final int insertIndex = findPlaceholderIndex(parentItem);
                parentItem.getChildren().add(insertIndex, newItem);
                parentItem.setExpanded(true);
            }
        }
    }
    
    private int findPlaceholderIndex(TreeItem<JsonNodeWithPath> parent)
    {
        final List<TreeItem<JsonNodeWithPath>> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++)
        {
            if (children.get(i) instanceof NavbarPlaceholderItem)
            {
                return i;
            }
        }
        return children.size();
    }
    
    @Override
    public void handlePathRemoved(String path)
    {
        visitedItemPaths.remove(path);
        final TreeItem<JsonNodeWithPath> itemToRemove = findNavbarItem(getRoot(), path);
        if (itemToRemove != null && itemToRemove.getParent() != null)
        {
            final TreeItem<JsonNodeWithPath> parent = itemToRemove.getParent();
            parent.getChildren().remove(itemToRemove);
            if (parent.getValue() != null && parent.getValue().isArray())
            {
                parent.setValue(model.getNodeForPath(parent.getValue().getPath()));
                parent.getChildren().clear();
                populateItem((NavbarItem) parent);
            }
            clearSelectionIfMatch(path);
        }
    }
    
    @Override
    public void handlePathChanged(String path)
    {
        updateSingleElement(path);
    }
    
    @Override
    public void handlePathMoved(String path)
    {
        repopulateChildren(path);
    }
    
    @Override
    public void handlePathSorted(String path)
    {
        repopulateChildren(path);
    }
    
    @Override
    public void handleRemovedSelection(String path)
    {
        clearSelectionIfMatch(path);
    }
    
    @Override
    public void handleSettingsChanged()
    {
        updateView();
    }
    
    private void clearSelectionIfMatch(String path)
    {
        if (path.equals(selectedPath))
        {
            getSelectionModel().clearSelection();
            selectedPath = null;
        }
    }
}
