package com.daniel.jsoneditor.view.testutil;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.JsonEditorEditorWindow;

import java.util.List;

/**
 * Reusable test double for {@link JsonEditorEditorWindow}.
 * Tracks {@link #flash()} calls and prevents live model queries by overriding path accessors
 * and navigation methods with configurable in-memory values.
 *
 * <p>Construct on the JavaFX Application Thread (e.g. via {@code WaitForAsyncUtils.asyncFx}).
 * Call {@link #resetFlash()} between scenarios in a multi-step test.
 */
public class TestEditorWindow extends JsonEditorEditorWindow
{
    private final String testSelectedPath;
    private final List<String> testOpenChildPaths;
    private boolean flashCalled = false;

    public TestEditorWindow(
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

    /** Returns {@code true} if {@link #flash()} has been called since construction or the last {@link #resetFlash()}. */
    public boolean isFlashCalled()
    {
        return flashCalled;
    }

    /** Resets the flash-called flag, allowing the same instance to be reused across multiple test scenarios. */
    public void resetFlash()
    {
        flashCalled = false;
    }
}
