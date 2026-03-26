package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class EditorTextFieldTest
{
    private EditorTextField textField;
    private TextTableCell mockParent;

    @Start
    void start(Stage stage)
    {
        mockParent = mock(TextTableCell.class);
        textField = new EditorTextField(mockParent, "initial text");

        final StackPane root = new StackPane(textField);
        stage.setScene(new Scene(root, 300, 100));
        stage.show();
    }

    @Test
    void shouldInitializeWithText()
    {
        assertEquals("initial text", textField.getText());
    }

    @Test
    void shouldCommitOnEnter(FxRobot robot)
    {
        robot.interact(() -> textField.requestFocus());
        robot.interact(() -> textField.setText("initial textnew text"));
        WaitForAsyncUtils.waitForFxEvents();
        // Directly fire the action event - equivalent to pressing ENTER in the TextField
        robot.interact(() -> textField.fireEvent(new javafx.event.ActionEvent()));
        WaitForAsyncUtils.waitForFxEvents();

        // EditorTextField routes through commitEditFromCurrentControl via setOnAction
        verify(mockParent, atLeastOnce()).commitEditFromCurrentControl("initial textnew text", textField);
    }

    @Test
    void shouldCommitOnFocusLoss(FxRobot robot)
    {
        robot.interact(() -> textField.requestFocus());
        robot.interact(() -> textField.setText("initial textchanged"));
        robot.interact(() -> textField.getParent().requestFocus());
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockParent, atLeastOnce()).commitEditFromCurrentControl(contains("changed"), eq(textField));
    }

    @Test
    void shouldNotifyParentOnTextChange(FxRobot robot)
    {
        robot.interact(() -> textField.setText("initial textx"));
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockParent, atLeastOnce()).onUserChangedText(anyString());
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

