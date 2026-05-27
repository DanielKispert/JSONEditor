package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.FlashHelper;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the editor window flash animation cleans up properly,
 * even when triggered multiple times in quick succession.
 */
@ExtendWith(ApplicationExtension.class)
class EditorWindowFlashTest
{
    private static final String FLASH_CLASS = "editor-window-flash";

    private FlashableVBox testNode;

    @Start
    void start(final Stage stage)
    {
        testNode = new FlashableVBox();
        stage.setScene(new Scene(new StackPane(testNode), 200, 100));
        stage.show();
    }

    /**
     * Full lifecycle: single flash adds at most one CSS class; rapid double-trigger does not accumulate;
     * after each animation completes the class is fully removed — verified at every stage.
     */
    @Test
    void flashAnimationCleansUpProperly() throws InterruptedException
    {
        // --- single flash: at most one CSS class entry is added immediately ---
        WaitForAsyncUtils.asyncFx(() -> testNode.flash());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(countFlashClasses() <= 1,
                "A single flash() must not add more than one CSS class instance, found: " + countFlashClasses());

        // wait for the animation to complete (3 keyframes × 200 ms = 600 ms, plus margin)
        Thread.sleep(900);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, countFlashClasses(),
                "Flash CSS class must be fully removed after animation completes");

        // --- rapid double-trigger: CSS class must not accumulate ---
        WaitForAsyncUtils.asyncFx(() ->
        {
            testNode.flash();
            testNode.flash();
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(countFlashClasses() <= 1,
                "Calling flash() twice rapidly must not accumulate CSS class entries, found: " + countFlashClasses());

        // wait for the animation to complete after the double-trigger as well
        Thread.sleep(900);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, countFlashClasses(),
                "All flash CSS class instances must be removed after animation, even after double-trigger");
    }

    private long countFlashClasses()
    {
        return testNode.getStyleClass().stream().filter(FLASH_CLASS::equals).count();
    }

    /**
     * Replicates the flash() logic from JsonEditorEditorWindow so the test
     * stays in sync with the production implementation.
     */
    static class FlashableVBox extends VBox
    {
        private Timeline flashTimeline;

        void flash()
        {
            flashTimeline = FlashHelper.flash(this, flashTimeline, FLASH_CLASS);
        }
    }
}
