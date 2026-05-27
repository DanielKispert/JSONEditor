package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.EditorScene;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManagerImpl;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that EditorWindowManagerImpl.focusExistingWindowForPath() only flashes a window
 * when the requested path is actually visible in that window — not merely when the parent path matches.
 */
@ExtendWith(ApplicationExtension.class)
class EditorWindowManagerFlashTest
{
    private EditorWindowManagerImpl editorWindowManager;
    private ReadableModel model;
    private Controller controller;
    private SettingsController settingsController;
    private EditorScene editorScene;

    @Start
    void start(Stage stage)
    {
        model = mock(ReadableModel.class);
        controller = mock(Controller.class);
        settingsController = mock(SettingsController.class);
        editorScene = mock(EditorScene.class);
        when(controller.getSettingsController()).thenReturn(settingsController);
        // Limit to 1 window so addWindow() is never called — avoids constructing a full UI window
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
        final TestWindow exactMatch = createTestWindow("/target", Collections.emptyList());
        addWindows(exactMatch);
        openPath("/target");
        assertTrue(exactMatch.isFlashCalled(),
                "flash() MUST fire when the exact path matches the window's selectedPath");
        assertEquals(1, windowCount(), "no new window when exact match found");

        clearWindows();

        // --- multiple windows: only the window with the exact match must flash ---
        final TestWindow nonTarget = createTestWindow("/other", Collections.emptyList());
        final TestWindow targetWindow = createTestWindow("/target", Collections.emptyList());
        addWindows(nonTarget, targetWindow);
        openPath("/target");
        assertFalse(nonTarget.isFlashCalled(), "wrong window must NOT be flashed");
        assertTrue(targetWindow.isFlashCalled(), "correct window MUST be flashed on exact path match");
        assertEquals(2, windowCount(), "no new window when exact match found");

        clearWindows();

        // --- bug regression: parent visible but child not in openChildPaths → no flash ---
        // Opening /root/name when the window shows /root should open a new window for the child,
        // not flash the root window (which does not display /root/name directly).
        final TestWindow rootWindow = createTestWindow("/root", Collections.emptyList());
        final TestWindow filler1 = createTestWindow("/filler1", Collections.emptyList());
        final TestWindow filler2 = createTestWindow("/filler2", Collections.emptyList());
        addWindows(rootWindow, filler1, filler2);
        openPath("/root/name");
        assertFalse(rootWindow.isFlashCalled(),
                "flash() must NOT fire when /root/name is not visible — user wants item details in a new window, not a highlight on the parent /root");
        assertFalse(filler1.isFlashCalled(), "filler must not be flashed");
        assertFalse(filler2.isFlashCalled(), "filler must not be flashed");
        assertEquals(3, windowCount(), "no new window added when at max capacity");

        clearWindows();

        // --- bug regression: parent array visible as child table, but the array item is not directly open → no flash ---
        // /processes is shown as a child table inside the window, but /processes/1 is a separate item
        // the user wants to navigate to — do not confuse the array table with the item record.
        final TestWindow processParent = createTestWindow("/root", List.of("/processes"));
        final TestWindow filler3 = createTestWindow("/filler1", Collections.emptyList());
        final TestWindow filler4 = createTestWindow("/filler2", Collections.emptyList());
        addWindows(processParent, filler3, filler4);
        openPath("/processes/1");
        assertFalse(processParent.isFlashCalled(),
                "flash() must NOT fire when /processes is a child table but /processes/1 is not — user wants the item record, not the array");
        assertFalse(filler3.isFlashCalled(), "filler must not be flashed");
        assertFalse(filler4.isFlashCalled(), "filler must not be flashed");
        assertEquals(3, windowCount(), "no new window added when at max capacity");

        clearWindows();

        // --- completely unrelated path → no flash ---
        final TestWindow unrelated = createTestWindow("/root", Collections.emptyList());
        final TestWindow filler5 = createTestWindow("/filler1", Collections.emptyList());
        final TestWindow filler6 = createTestWindow("/filler2", Collections.emptyList());
        addWindows(unrelated, filler5, filler6);
        openPath("/other/thing");
        assertFalse(unrelated.isFlashCalled(),
                "flash() must not fire when no path relationship exists at all");
        assertFalse(filler5.isFlashCalled(), "filler must not be flashed");
        assertFalse(filler6.isFlashCalled(), "filler must not be flashed");
        assertEquals(3, windowCount(), "no new window added when at max capacity");
    }

    /**
     * A path listed in a window's openChildPaths is directly rendered as a child table inside that
     * window — opening it must flash the window (already visible) rather than open a new one.
     */
    @Test
    void flashWhenPathIsOpenChildTable()
    {
        final TestWindow window = createTestWindow("/root", List.of("/root/child"));
        addWindows(window);
        openPath("/root/child");
        assertTrue(window.isFlashCalled(),
                "flash() MUST fire when the exact path is in openChildPaths — it is already rendered as a child table in this window");
        assertEquals(1, windowCount(), "no new window when exact match found in openChildPaths");
    }

    // --- helpers ---

    private void addWindows(final TestWindow... windows)
    {
        WaitForAsyncUtils.asyncFx(() ->
        {
            for (final TestWindow w : windows)
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

    private TestWindow createTestWindow(final String selectedPath, final List<String> openChildPaths)
    {
        final TestWindow[] result = new TestWindow[1];
        WaitForAsyncUtils.asyncFx(
                () -> result[0] = new TestWindow(editorWindowManager, model, controller, selectedPath, openChildPaths));
        WaitForAsyncUtils.waitForFxEvents();
        return result[0];
    }

    /**
     * A minimal test double for JsonEditorEditorWindow that tracks flash() calls and prevents
     * live model queries by overriding path accessors and navigation methods.
     */
    static class TestWindow extends JsonEditorEditorWindow
    {
        private final String testSelectedPath;
        private final List<String> testOpenChildPaths;
        private boolean flashCalled = false;

        TestWindow(
                final EditorWindowManager manager,
                final ReadableModel model,
                final Controller controller,
                final String selectedPath,
                final List<String> openChildPaths)
        {
            super(manager, model, controller);
            this.testSelectedPath = selectedPath;
            this.testOpenChildPaths = openChildPaths;
        }

        @Override
        public String getSelectedPath()
        {
            return testSelectedPath;
        }

        @Override
        public List<String> getOpenChildPaths()
        {
            return testOpenChildPaths;
        }

        @Override
        public void flash()
        {
            flashCalled = true;
        }

        @Override
        public void focusArrayItem(final String path)
        {
            // no-op: prevents navigation side-effects during test
        }

        @Override
        public void setSelectedPath(final String path, final boolean openObjectParentOfArray)
        {
            // no-op: prevents model queries during fallback path assignment
        }

        @Override
        public void setSelectedPath(final String path)
        {
            // no-op
        }

        public boolean isFlashCalled()
        {
            return flashCalled;
        }
    }
}
