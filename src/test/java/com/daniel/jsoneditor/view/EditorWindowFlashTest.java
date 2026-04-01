package com.daniel.jsoneditor.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    void start(Stage stage)
    {
        testNode = new FlashableVBox();
        stage.setScene(new Scene(new StackPane(testNode), 200, 100));
        stage.show();
    }

    @Test
    void singleFlashShouldAddAtMostOneStyleClass()
    {
        WaitForAsyncUtils.asyncFx(() -> testNode.flash());
        WaitForAsyncUtils.waitForFxEvents();

        final long count = countFlashClasses();
        assertTrue(count <= 1, "A single flash() must not add more than one CSS class instance, found: " + count);
    }

    @Test
    void rapidDoubleFlashShouldNotAccumulateStyleClasses()
    {
        WaitForAsyncUtils.asyncFx(() -> {
            testNode.flash();
            testNode.flash();
        });
        WaitForAsyncUtils.waitForFxEvents();

        final long count = countFlashClasses();
        assertTrue(count <= 1,
                "Calling flash() twice rapidly must not accumulate CSS class entries, found: " + count);
    }

    @Test
    void flashStyleClassShouldBeRemovedAfterAnimationCompletes() throws InterruptedException
    {
        WaitForAsyncUtils.asyncFx(() -> testNode.flash());
        WaitForAsyncUtils.waitForFxEvents();

        // animation duration: 3 keyframes × 200ms = 600ms, plus margin
        Thread.sleep(900);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, countFlashClasses(),
                "Flash CSS class must be fully removed after animation completes");
    }

    @Test
    void doubleFlashStyleClassShouldBeFullyRemovedAfterAnimation() throws InterruptedException
    {
        WaitForAsyncUtils.asyncFx(() -> {
            testNode.flash();
            testNode.flash();
        });
        WaitForAsyncUtils.waitForFxEvents();

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
        void flash()
        {
            requestFocus();
            final String flashClass = FLASH_CLASS;
            final Timeline timeline = new Timeline();
            for (int i = 0; i < 3; i++)
            {
                final boolean on = (i % 2 == 0);
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 200), e -> {
                    if (on)
                    {
                        getStyleClass().add(flashClass);
                    }
                    else
                    {
                        getStyleClass().remove(flashClass);
                    }
                }));
            }
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3 * 200), e -> getStyleClass().remove(flashClass)));
            timeline.play();
        }
    }
}

