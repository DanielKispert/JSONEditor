package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ReorderButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.VisibilityToggleButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.JsonEditorNamebar;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.EditorTableView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(ApplicationExtension.class)
class JsonEditorNamebarTest
{
    private JsonEditorNamebar namebar;
    private SettingsController settingsController;
    private JsonNodeWithPath arrayNodeWithPath;
    private JsonNodeWithPath objectNodeWithPath;

    @Start
    void start(Stage stage)
    {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        final ArrayNode array = mapper.createArrayNode();
        array.add(mapper.createObjectNode());

        final JsonNodeWithPath rootNodeWithPath = new JsonNodeWithPath(root, "");
        arrayNodeWithPath = new JsonNodeWithPath(array, "/items");
        objectNodeWithPath = new JsonNodeWithPath(mapper.createObjectNode(), "/config");

        final ReadableModel model = Mockito.mock(ReadableModel.class);
        Mockito.when(model.isGitBlameAvailable()).thenReturn(false);
        Mockito.when(model.getNodeForPath(ArgumentMatchers.eq(""))).thenReturn(rootNodeWithPath);
        Mockito.when(model.getNodeForPath(ArgumentMatchers.eq("/items"))).thenReturn(arrayNodeWithPath);
        Mockito.when(model.getNodeForPath(ArgumentMatchers.eq("/config"))).thenReturn(objectNodeWithPath);
        Mockito.when(model.getReferenceableObject(ArgumentMatchers.anyString())).thenReturn(null);

        final Controller controller = Mockito.mock(Controller.class);
        settingsController = Mockito.mock(SettingsController.class);
        Mockito.when(controller.getSettingsController()).thenReturn(settingsController);

        final EditorTableView mainTableView = Mockito.mock(EditorTableView.class);
        Mockito.when(mainTableView.isTemporaryShowAllColumns()).thenReturn(false);

        namebar = new JsonEditorNamebar(
            Mockito.mock(EditorWindowManager.class), Mockito.mock(JsonEditorEditorWindow.class),
            model, controller, mainTableView);

        stage.show();
    }

    /**
     * Verifies that ReorderButton and VisibilityToggleButton respond correctly to
     * node type (array vs object) and settings (hideEmptyColumns).
     */
    @Test
    void namebarButtonsRespondToSelectionAndSettings()
    {
        // Array selected → reorder button visible
        Mockito.when(settingsController.hideEmptyColumns()).thenReturn(false);
        WaitForAsyncUtils.asyncFx(() -> namebar.setSelection(arrayNodeWithPath));
        WaitForAsyncUtils.waitForFxEvents();
        final ReorderButton reorderBtn = findChildOfType(namebar, ReorderButton.class);
        assertNotNull(reorderBtn, "ReorderButton must exist");
        assertTrue(reorderBtn.isVisible() && reorderBtn.isManaged(),
            "ReorderButton should be visible for arrays");

        // Object selected → reorder button hidden
        WaitForAsyncUtils.asyncFx(() -> namebar.setSelection(objectNodeWithPath));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(!reorderBtn.isVisible() && !reorderBtn.isManaged(),
            "ReorderButton should be hidden for objects");

        // Array + hideEmptyColumns enabled → visibility toggle visible
        Mockito.when(settingsController.hideEmptyColumns()).thenReturn(true);
        WaitForAsyncUtils.asyncFx(() -> namebar.setSelection(arrayNodeWithPath));
        WaitForAsyncUtils.waitForFxEvents();
        final VisibilityToggleButton toggleBtn = findChildOfType(namebar, VisibilityToggleButton.class);
        assertNotNull(toggleBtn, "VisibilityToggleButton must exist");
        assertTrue(toggleBtn.isVisible() && toggleBtn.isManaged(),
            "VisibilityToggleButton should be visible when setting enabled + array selected");

        // Array + hideEmptyColumns disabled → visibility toggle hidden
        Mockito.when(settingsController.hideEmptyColumns()).thenReturn(false);
        WaitForAsyncUtils.asyncFx(() -> namebar.setSelection(arrayNodeWithPath));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(!toggleBtn.isVisible() && !toggleBtn.isManaged(),
            "VisibilityToggleButton should be hidden when setting disabled");
    }

    private <T> T findChildOfType(final Pane parent, final Class<T> type)
    {
        return parent.getChildrenUnmodifiable().stream()
            .filter(type::isInstance)
            .map(type::cast)
            .findFirst()
            .orElse(null);
    }
}
