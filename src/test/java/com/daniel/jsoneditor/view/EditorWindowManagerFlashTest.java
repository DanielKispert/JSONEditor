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
        final TestEditorWindow exactMatch = createTestWindow("/target", NO_CHILD_PATHS);
        addWindows(exactMatch);
        openPath("/target");
        assertTrue(exactMatch.isFlashCalled(),
                "flash() MUST fire when the exact path matches the window's selectedPath");
        assertEquals(1, windowCount(), "no new window when exact match found");

        clearWindows();

        final TestEditorWindow nonTarget = createTestWindow("/other", NO_CHILD_PATHS);
        final TestEditorWindow targetWindow = createTestWindow("/target", NO_CHILD_PATHS);
        addWindows(nonTarget, targetWindow);
        openPath("/target");
        assertFalse(nonTarget.isFlashCalled(), "wrong window must NOT be flashed");
        assertTrue(targetWindow.isFlashCalled(), "correct window MUST be flashed on exact path match");
        assertEquals(2, windowCount(), "no new window when exact match found");

        clearWindows();

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

        when(model.getNodeForPath("/items/0")).thenReturn(
                new JsonNodeWithPath(JsonNodeFactory.instance.textNode("foo"), "/items/0"));
        openPath("/items/0");
        assertTrue(arrayWindow.isFlashCalled(),
                "flash() MUST fire when navigating to a primitive array item whose parent array is already open");
        assertTrue(arrayWindow.isFocusArrayItemCalled(),
                "focusArrayItem() must be called: primitive item is rendered inline in the parent array table");
        assertEquals(1, windowCount(), "no new window: primitive item shown in existing parent window");
        arrayWindow.resetFlash();

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

        clearWindows();
        final TestEditorWindow[] fillers = createFillerWindows();
        addWindows(arrayWindow, fillers[0], fillers[1]);
        arrayWindow.resetFlash();

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

    /**
     * Exercises all close-group operations in a realistic flow, and verifies the ordering query methods
     * that drive enable/disable logic in the context menu.
     */
    @Test
    void closeWindowGroupOperations_flowAcrossMultipleWindows()
    {
        // Phase 1: four windows open — validate query state
        final TestEditorWindow leftWindow = createTestWindow("/left", NO_CHILD_PATHS);
        final TestEditorWindow refWindow = createTestWindow("/ref", NO_CHILD_PATHS);
        final TestEditorWindow rightA = createTestWindow("/rightA", NO_CHILD_PATHS);
        final TestEditorWindow rightB = createTestWindow("/rightB", NO_CHILD_PATHS);
        addWindows(leftWindow, refWindow, rightA, rightB);

        assertEquals(4, editorWindowManager.getOpenWindowCount(), "four windows open initially");
        assertTrue(editorWindowManager.hasWindowsToTheLeft(refWindow), "ref has windows to its left");
        assertTrue(editorWindowManager.hasWindowsToTheRight(refWindow), "ref has windows to its right");
        assertFalse(editorWindowManager.hasWindowsToTheLeft(leftWindow), "leftmost has no windows to its left");
        assertFalse(editorWindowManager.hasWindowsToTheRight(rightB), "rightmost has no windows to its right");

        // Phase 2: closeWindowsToTheRight — keeps ref and everything left
        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeWindowsToTheRight(refWindow));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(2, windowCount(), "leftWindow + refWindow remain after closeRight");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(leftWindow), "leftWindow survives closeRight");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(refWindow), "refWindow survives closeRight");
        assertFalse(editorWindowManager.hasWindowsToTheRight(refWindow), "refWindow is now rightmost");

        // Phase 3: re-populate, closeWindowsToTheLeft — keeps ref and everything right
        clearWindows();
        addWindows(leftWindow, refWindow, rightA, rightB);

        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeWindowsToTheLeft(refWindow));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(3, windowCount(), "refWindow + rightA + rightB remain after closeLeft");
        assertFalse(editorWindowManager.getEditorWindowContainer().getItems().contains(leftWindow), "leftWindow removed by closeLeft");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(refWindow), "refWindow survives closeLeft");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(rightA), "rightA survives closeLeft");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(rightB), "rightB survives closeLeft");
        assertFalse(editorWindowManager.hasWindowsToTheLeft(refWindow), "refWindow is now leftmost");

        // Phase 4: re-populate, closeOtherWindows — keeps only the named window
        clearWindows();
        addWindows(leftWindow, refWindow, rightA, rightB);

        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeOtherWindows(leftWindow));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, windowCount(), "only leftWindow survives closeOtherWindows");
        assertTrue(editorWindowManager.getEditorWindowContainer().getItems().contains(leftWindow), "leftWindow is sole survivor");
        assertEquals(1, editorWindowManager.getOpenWindowCount(), "getOpenWindowCount reflects single survivor");

        // Phase 5: closeAllWindows — empties the list
        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeAllWindows());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, windowCount(), "closeAllWindows must empty the list");
        assertEquals(0, editorWindowManager.getOpenWindowCount(), "getOpenWindowCount is 0 after closeAll");

        // Phase 6: no-op edge case — reference window not in list
        addWindows(leftWindow, rightA);
        final TestEditorWindow ghost = createTestWindow("/ghost", NO_CHILD_PATHS);

        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeWindowsToTheRight(ghost));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, windowCount(), "closeWindowsToTheRight with absent window must not change list");

        WaitForAsyncUtils.asyncFx(() -> editorWindowManager.closeWindowsToTheLeft(ghost));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, windowCount(), "closeWindowsToTheLeft with absent window must not change list");

        assertFalse(editorWindowManager.hasWindowsToTheRight(ghost), "absent window reports false for hasWindowsToTheRight");
        assertFalse(editorWindowManager.hasWindowsToTheLeft(ghost), "absent window reports false for hasWindowsToTheLeft");

        clearWindows();
    }
}
