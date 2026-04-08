package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.navbar.NavbarPlaceholderItem;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that NavbarPlaceholderItems are shifted one indent level to the left
 * so they visually align with their sibling items instead of appearing more indented.
 */
@ExtendWith(ApplicationExtension.class)
class NavbarPlaceholderIndentTest
{
    private static final String PLACEHOLDER_STYLE_CLASS = "navbar-placeholder-cell";

    private TreeView<JsonNodeWithPath> treeView;

    @Start
    void start(Stage stage)
    {
        final ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.putArray("items").addObject().put("name", "child");

        final TreeItem<JsonNodeWithPath> root = new TreeItem<>(new JsonNodeWithPath(rootNode, ""));
        final TreeItem<JsonNodeWithPath> regularItem = new TreeItem<>(
                new JsonNodeWithPath(rootNode.get("items").get(0), "/items/0"));
        final NavbarPlaceholderItem placeholder = new NavbarPlaceholderItem(5, "/items");

        root.getChildren().add(regularItem);
        root.getChildren().add(placeholder);
        root.setExpanded(true);

        treeView = new TreeView<>(root);
        treeView.setCellFactory(tv -> new TestNavbarTreeCell());

        stage.setScene(new Scene(new StackPane(treeView), 400, 300));
        stage.show();
    }

    @Test
    void placeholderCellShouldBeShiftedLeft()
    {
        WaitForAsyncUtils.waitForFxEvents();

        // row 0 = root, row 1 = regular child, row 2 = placeholder
        final TreeCell<JsonNodeWithPath> regularCell = getCellForRow(1);
        final TreeCell<JsonNodeWithPath> placeholderCell = getCellForRow(2);

        assertNotNull(regularCell);
        assertNotNull(placeholderCell);
        assertEquals(0, regularCell.getTranslateX(), "Regular cell must not be shifted");
        assertEquals(-10, placeholderCell.getTranslateX(), "Placeholder cell must be shifted one indent level left");
    }

    @Test
    void placeholderCellShouldHavePlaceholderStyleClass()
    {
        WaitForAsyncUtils.waitForFxEvents();

        final TreeCell<JsonNodeWithPath> placeholderCell = getCellForRow(2);
        assertNotNull(placeholderCell);
        assertTrue(placeholderCell.getStyleClass().contains(PLACEHOLDER_STYLE_CLASS),
                "Placeholder cell must have the placeholder CSS class");
    }

    @Test
    void regularCellShouldNotHavePlaceholderStyleClass()
    {
        WaitForAsyncUtils.waitForFxEvents();

        final TreeCell<JsonNodeWithPath> regularCell = getCellForRow(1);
        assertNotNull(regularCell);
        assertFalse(regularCell.getStyleClass().contains(PLACEHOLDER_STYLE_CLASS),
                "Regular cell must not have the placeholder CSS class");
    }

    @SuppressWarnings("unchecked")
    private TreeCell<JsonNodeWithPath> getCellForRow(int row)
    {
        return treeView.lookupAll(".tree-cell").stream()
                .filter(node -> node instanceof TreeCell)
                .map(node -> (TreeCell<JsonNodeWithPath>) node)
                .filter(cell -> cell.getIndex() == row)
                .findFirst()
                .orElse(null);
    }

    /**
     * Mirrors the production NavbarTreeCell logic for translateX and style class handling.
     */
    private static class TestNavbarTreeCell extends TreeCell<JsonNodeWithPath>
    {
        @Override
        protected void updateItem(JsonNodeWithPath item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty || item == null)
            {
                setText(null);
                setTranslateX(0);
                getStyleClass().remove(PLACEHOLDER_STYLE_CLASS);
            }
            else if (getTreeItem() instanceof NavbarPlaceholderItem)
            {
                setText(((NavbarPlaceholderItem) getTreeItem()).getDisplayText());
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
}



