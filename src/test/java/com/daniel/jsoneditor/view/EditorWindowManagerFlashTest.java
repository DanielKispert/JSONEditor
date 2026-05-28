package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManagerImpl;
import com.daniel.jsoneditor.view.testutil.TestEditorWindow;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that EditorWindowManagerImpl.focusExistingWindowForPath() only flashes a window
 * when the requested path is actually visible in that window — not merely when the parent path matches.
 */
@ExtendWith(ApplicationExtension.class)
class EditorWindowManagerFlashTest
{
    private static final List<String> NO_CHILD_PATHS = List.of();

    private EditorWindowManagerImpl editorWindowManager;
    private ReadableModel model;
    private Controller controller;
    private SettingsController settingsController;
    private EditorScene editorScene;

    @Start
    void start(final Stage stage)
    {
        model = mock(ReadableModel.class);
        controller = mock(Controller.class);
        settingsController = mock(SettingsController.class);
        editorScene = mock(EditorScene.class);
        when(controller.getSettingsController()).thenReturn(settingsController);
        // "1" is clamped to 3 by getMaxWindows()'s Math.max(3, ...) floor.
        // Bug-regression scenarios fill all 3 slots so canAnotherWindowBeAdded() stays false --
        // no real JsonEditorEditorWindow is ever constructed.
        when(settingsController.getMaxEditorWindows()).thenReturn("1");
        editorWindowManager = new EditorWindowManagerImpl(editorScene, model, controller);
        stage.setScene(new Scene(new StackPane(editorWindowManager.getEditorWindowContainer()), 800, 600));
        stage.show();
    }

    /**
     * Full flash/no-flash lifecycle: exact-path match fires flash; parent-path or child-table
     * prefix matches do not — the user wants to open the requested node, not highlight an ancestor.
     *
     * Covers: exact selectedPath match, multi-window selectivity, two bug-regression
     * scenarios (parent-prefix and child-table-prefix), and unrelated-path baseline.
     */
    @Test
    void flashOnlyWhenExactPathAlreadyOpen()
    {
        // --- exact selectedPath match → flash, no new window ---
        final TestEditorWindow exactMatch = createTestWindow("/target", NO_CHILD_PATHS);
        addWindows(exactMatch);
        openPath("/target");
        assertTrue(exactMatch.isFlashCalled(),
                "flash() MUST fire when the exact path matches the window's selectedPath");
        assertEquals(1, windowCount(), "no new window when exact match found");

        clearWindows();

        // --- multiple windows: only the window with the exact match must flash ---
        final TestEditorWindow nonTarget = createTestWindow("/other", NO_CHILD_PATHS);
        final TestEditorWindow targetWindow = createTestWindow("/target", NO_CHILD_PATHS);
        addWindows(nonTarget, targetWindow);
        openPath("/target");
        assertFalse(nonTarget.isFlashCalled(), "wrong window must NOT be flashed");
        assertTrue(targetWindow.isFlashCalled(), "correct window MUST be flashed on exact path match");
        assertEquals(2, windowCount(), "no new window when exact match found");

        clearWindows();

        // --- bug regression: parent visible but child not in openChildPaths → no flash ---
        // Opening /root/name when the window shows /root should open a new window for the child,
        // not flash the root window (which does not display /root/name directly).
        final TestEditorWindow rootWindow = createTestWindow("/root", NO_CHILD_PATHS);
        final TestEditorWindow[] fillers1 = createFillerWindows();
        addWindows(rootWindow, fillers1[0], fillers1[1]);
        openPath("/root/name");
        assertFalse(rootWindow.isFlashCalled(),
                "flash() must NOT fire when /root/name is not visible in the window; "
                        + "user wants to open the child in a new window, not highlight the parent /root");
        assertNoneFlashed("parent-path scenario", fillers1[0], fillers1[1]);
        assertEquals(3, windowCount(), "no new window added when at max capacity");

        clearWindows();

        // --- bug regression: parent array visible as child table, but the array item is not directly open → no flash ---
        // /processes is shown as a child table inside the window, but /processes/1 is a separate item
        // the user wants to navigate to — do not confuse the array table with the item record.
        final TestEditorWindow processParent = createTestWindow("/root", List.of("/processes"));
        final TestEditorWindow[] fillers2 = createFillerWindows();
        addWindows(processParent, fillers2[0], fillers2[1]);
        openPath("/processes/1");
        assertFalse(processParent.isFlashCalled(),
                "flash() must NOT fire when /processes is a child table but /processes/1 is not -- "
                        + "user wants the item record, not the array");
        assertNoneFlashed("child-table scenario", fillers2[0], fillers2[1]);
        assertEquals(3, windowCount(), "no new window added when at max capacity");

        clearWindows();

        // --- completely unrelated path → no flash ---
        final TestEditorWindow unrelated = createTestWindow("/root", NO_CHILD_PATHS);
        final TestEditorWindow[] fillers3 = createFillerWindows();
        addWindows(unrelated, fillers3[0], fillers3[1]);
        openPath("/other/thing");
        assertFalse(unrelated.isFlashCalled(),
                "flash() must not fire when no path relationship exists at all");
        assertNoneFlashed("unrelated-path scenario", fillers3[0], fillers3[1]);
        assertEquals(3, windowCount(), "no new window added when at max capacity");
    }

    /**
     * A path listed in a window's openChildPaths is directly rendered as a child table inside that
     * window — opening it must flash the window (already visible) rather than open a new one.
     */
    @Test
    void flashWhenPathIsOpenChildTable()
    {
        final TestEditorWindow childTableWindow = createTestWindow("/root", List.of("/root/child"));
        addWindows(childTableWindow);
        openPath("/root/child");
        assertTrue(childTableWindow.isFlashCalled(),
                "flash() MUST fire when the exact path is in openChildPaths -- it is already rendered as a child table in this window");
        assertTrue(childTableWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must be called when path matches an open child table");
        assertEquals(1, windowCount(), "no new window when exact match found in openChildPaths");
    }

    // --- helpers ---

    private TestEditorWindow[] createFillerWindows()
    {
        return new TestEditorWindow[]{
            createTestWindow("/filler1", NO_CHILD_PATHS),
            createTestWindow("/filler2", NO_CHILD_PATHS)
        };
    }

    private void assertNoneFlashed(final String context, final TestEditorWindow... windows)
    {
        for (final TestEditorWindow w : windows)
        {
            assertFalse(w.isFlashCalled(), context + ": window at " + w.getSelectedPath() + " must not be flashed");
        }
    }

    private void addWindows(final TestEditorWindow... windows)
    {
        WaitForAsyncUtils.asyncFx(() ->
        {
            for (final TestEditorWindow w : windows)
            {
                editorWindowManager.getEditorWindowContainer().getItems().add(w);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clearWindows()
    {
        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.getEditorWindowContainer().getItems().clear());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void openPath(final String path)
    {
        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.openPath(path, false));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private int windowCount()
    {
        return editorWindowManager.getEditorWindowContainer().getItems().size();
    }

    private TestEditorWindow createTestWindow(final String selectedPath, final List<String> openChildPaths)
    {
        final TestEditorWindow[] result = new TestEditorWindow[1];
        WaitForAsyncUtils.asyncFx(
                () -> result[0] = new TestEditorWindow(editorWindowManager, model, controller, selectedPath, openChildPaths));
        WaitForAsyncUtils.waitForFxEvents();
        return result[0];
    }

    @Test
    void parentArrayFallback_flashesOnlyForNonNavigableItems()
    {
        when(model.getNodeForPath("/items")).thenReturn(
                new JsonNodeWithPath(JsonNodeFactory.instance.arrayNode(), "/items"));
        final TestEditorWindow arrayWindow = createTestWindow("/items", NO_CHILD_PATHS);
        addWindows(arrayWindow);

        // --- sub-scenario 1: primitive item → flash + focusArrayItem, no new window ---
        when(model.getNodeForPath("/items/0")).thenReturn(
                new JsonNodeWithPath(JsonNodeFactory.instance.textNode("foo"), "/items/0"));
        openPath("/items/0");
        assertTrue(arrayWindow.isFlashCalled(),
                "flash() MUST fire when navigating to a primitive array item whose parent array is already open");
        assertTrue(arrayWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must be called: primitive item is rendered inline in the parent array table");
        assertEquals(1, windowCount(), "no new window: primitive item shown in existing parent window");
        arrayWindow.resetFlash();

        // --- sub-scenario 2: flat-object item → flash + focusArrayItem, no new window ---
        final ObjectNode flatObject = JsonNodeFactory.instance.objectNode();
        flatObject.put("name", "foo");
        flatObject.put("count", 42);
        when(model.getNodeForPath("/items/1")).thenReturn(new JsonNodeWithPath(flatObject, "/items/1"));
        openPath("/items/1");
        assertTrue(arrayWindow.isFlashCalled(),
                "flash() MUST fire when navigating to a flat-object array item whose parent array is already open");
        assertTrue(arrayWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must be called: flat-object item is rendered as a row in the parent array table");
        assertEquals(1, windowCount(), "no new window: flat-object item shown in existing parent window");

        // fill capacity to prevent a new window from opening in sub-scenario 3
        clearWindows();
        final TestEditorWindow[] fillers = createFillerWindows();
        addWindows(arrayWindow, fillers[0], fillers[1]);
        arrayWindow.resetFlash();

        // --- sub-scenario 3: complex-object item → NO flash, no focusArrayItem ---
        final ObjectNode complexObject = JsonNodeFactory.instance.objectNode();
        complexObject.put("name", "Alice");
        complexObject.set("hobbies", JsonNodeFactory.instance.arrayNode());
        when(model.getNodeForPath("/items/2")).thenReturn(new JsonNodeWithPath(complexObject, "/items/2"));
        openPath("/items/2");
        assertFalse(arrayWindow.isFlashCalled(),
                "flash() must NOT fire for a complex object that deserves its own editor window");
        assertFalse(arrayWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must NOT be called: complex item should open in a new window, not be focused inline");
        assertNoneFlashed("complex-object sub-scenario", fillers[0], fillers[1]);
        assertEquals(3, windowCount(), "no new window added when at max capacity");
    }

    @Test
    void childTableFallback_flashesForItemsInOpenChildPaths()
    {
        // The window shows /persons/0 and has /persons/0/hobbies rendered as an inline child table.
        // Navigating to an item inside that child array must flash this window and focus the row.
        when(model.getNodeForPath("/persons/0/hobbies")).thenReturn(
                new JsonNodeWithPath(JsonNodeFactory.instance.arrayNode(), "/persons/0/hobbies"));
        when(model.getNodeForPath("/persons/0/hobbies/2")).thenReturn(
                new JsonNodeWithPath(JsonNodeFactory.instance.textNode("painting"), "/persons/0/hobbies/2"));

        final TestEditorWindow objectWindow = createTestWindow("/persons/0", List.of("/persons/0/hobbies"));
        addWindows(objectWindow);
        openPath("/persons/0/hobbies/2");

        assertTrue(objectWindow.isFlashCalled(),
                "flash() MUST fire when navigating to a primitive item whose parent array is a child table in an open window");
        assertTrue(objectWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must be called: item is rendered inline in the child table already visible in this window");
        assertEquals(1, windowCount(), "no new window: item is visible via child table in existing window");
    }
}
