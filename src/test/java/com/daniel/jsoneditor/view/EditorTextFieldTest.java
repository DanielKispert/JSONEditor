package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.fields.EditorTextField;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

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
        robot.clickOn(textField);
        robot.write("new text");
        robot.type(KeyCode.ENTER);
        
        verify(mockParent, atLeastOnce()).commitEdit("initial textnew text");
    }
    
    @Test
    void shouldCommitOnFocusLoss(FxRobot robot)
    {
        robot.clickOn(textField);
        robot.write("changed");
        robot.clickOn(300, 300);
        
        verify(mockParent, atLeastOnce()).commitEdit(contains("changed"));
    }
    
    @Test
    void shouldNotifyParentOnTextChange(FxRobot robot)
    {
        robot.clickOn(textField);
        robot.write("x");
        
        verify(mockParent, atLeastOnce()).onUserChangedText(anyString());
    }
    
    @Test
    void shouldAdjustWidthBasedOnContent()
    {
        final double initialWidth = textField.getPrefWidth();
        textField.setText("this is a much longer text that should increase the width");
        final double newWidth = textField.getPrefWidth();
        
        assertTrue(newWidth > initialWidth);
    }
}

