package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class EditorTextFieldTest
{
    private EditorTextField textField;
    private TextTableCell mockParent;

    @Start
    void start(final Stage stage)
    {
        mockParent = Mockito.mock(TextTableCell.class);
        textField = new EditorTextField(mockParent, "initial text");
        final VBox root = new VBox(textField);
        stage.setScene(new Scene(root, 300, 200));
        stage.show();
    }

    @Test
    void shouldInitializeWithText()
    {
        assertEquals("initial text", textField.getText());
    }

    @Test
    void shouldCommitOnEnter(final FxRobot robot)
    {
        robot.interact(() -> textField.requestFocus());
        robot.interact(() -> textField.setText("initial textnew text"));
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> textField.fireEvent(new ActionEvent()));
        WaitForAsyncUtils.waitForFxEvents();

        Mockito.verify(mockParent, Mockito.atLeastOnce()).commitEditFromCurrentControl("initial textnew text",
                textField);
    }

    /**
     * Tests that the commit path (used by focus loss, Enter, and action events) correctly
     * forwards the current text to the parent cell. The commit logic was extracted into
     * {@link EditorTextField#commitEdit()} to make it directly testable without depending
     * on OS-level focus transfer, which is unreliable in automated test environments.
     */
    @Test
    void shouldCommitCurrentTextViaCommitEdit(final FxRobot robot)
    {
        robot.interact(() -> textField.setText("initial textchanged"));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> textField.commitEdit());
        WaitForAsyncUtils.waitForFxEvents();

        Mockito.verify(mockParent, Mockito.atLeastOnce()).commitEditFromCurrentControl("initial textchanged",
                textField);
    }

    @Test
    void shouldNotifyParentOnTextChange(final FxRobot robot)
    {
        robot.interact(() -> textField.setText("initial textx"));
        WaitForAsyncUtils.waitForFxEvents();

        Mockito.verify(mockParent, Mockito.atLeastOnce()).onUserChangedText(Mockito.anyString());
    }

    @Test
    void shouldAdjustWidthBasedOnContent()
    {
        WaitForAsyncUtils.asyncFx(() -> {
            final double initialWidth = textField.getPrefWidth();
            textField.setText("this is a much longer text that should increase the width");
            assertTrue(textField.getPrefWidth() > initialWidth);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
}
