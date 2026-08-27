package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.settings.SettingsController;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.EditorTableCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells.TextTableCell;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns.EditorTableColumn;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests that focus-loss on an empty, never-committed required field does NOT produce a spurious commit.
 *
 * Root cause on main:
 *   EditorTableCell.commitEdit guards with Objects.equals(newValue, committedValue).
 *   When committedValue == null and newValue == "" the guard evaluates to false (not equal),
 *   so execution continues and saveValue is called for the empty string on a required column.
 */
@ExtendWith(ApplicationExtension.class)
class EditorTableCellTest
{
    private Controller mockController;
    private TextTableCell cell;
    private Control inputControl;

    @SuppressWarnings("unchecked")
    @Start
    void start(final Stage stage) throws Exception
    {
        mockController = Mockito.mock(Controller.class);
        final SettingsController mockSettings = Mockito.mock(SettingsController.class);
        Mockito.when(mockController.getSettingsController()).thenReturn(mockSettings);
        final ReadableModel mockModel = Mockito.mock(ReadableModel.class);
        final EditorWindowManager mockManager = Mockito.mock(EditorWindowManager.class);

        // Required column whose JSON property does not yet exist in the row item
        final EditorTableColumn mockColumn = Mockito.mock(EditorTableColumn.class);
        Mockito.when(mockColumn.isRequired()).thenReturn(true);
        Mockito.when(mockColumn.getPropertyName()).thenReturn("name");

        // Row item: real ObjectNode that has no "name" property  →  jsonNode == null in commitEdit
        final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        final JsonNodeWithPath mockItem = Mockito.mock(JsonNodeWithPath.class);
        Mockito.when(mockItem.getNode()).thenReturn(objectNode);
        Mockito.when(mockItem.getPath()).thenReturn("/items/0");

        final TableRow<JsonNodeWithPath> mockTableRow = Mockito.mock(TableRow.class);
        Mockito.when(mockTableRow.getItem()).thenReturn(mockItem);

        // Create the cell. committedValue stays null (Java default) — simulates a field never committed.
        cell = new TextTableCell(mockManager, mockController, mockModel, false, false);

        // Inject mockTableRow via TableCell's private ReadOnlyObjectWrapper<TableRow> field.
        // The --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED JVM arg (in build.gradle)
        // allows setAccessible(true) on this private field from our test module.
        final Field tableRowField = TableCell.class.getDeclaredField("tableRow");
        tableRowField.setAccessible(true);
        ((ReadOnlyObjectWrapper<TableRow<JsonNodeWithPath>>) tableRowField.get(cell)).set(mockTableRow);

        // Inject mockColumn after tableRow so that updateColumnIndex() (triggered by invalidation)
        // finds a null tableView rather than NPE-ing on the row-to-table path.
        final Field tableColumnField = TableCell.class.getDeclaredField("tableColumn");
        tableColumnField.setAccessible(true);
        ((ReadOnlyObjectWrapper<TableColumn<JsonNodeWithPath, String>>) tableColumnField.get(cell)).set(mockColumn);

        // Simulate a text input control that was set during updateItem (normally on cell render).
        inputControl = new TextField();
        final Field ctrlField = EditorTableCell.class.getDeclaredField("currentTextInputControl");
        ctrlField.setAccessible(true);
        ctrlField.set(cell, inputControl);
    }

    /**
     * Simulates focus loss on an empty required field that was never previously committed
     * (committedValue == null, the JSON property does not yet exist on the row item).
     *
     * Expected: controller.setValueAtPath is NEVER called because saving "" for a
     * required field that never had a value is meaningless and should be suppressed.
     *
     * On main this test is RED: setValueAtPath IS called with "" because
     * Objects.equals("", null) == false bypasses the equality guard, and the
     * isRequired() branch calls saveValue("") unconditionally.
     */
    @Test
    void focusLossOnEmptyRequiredFieldDoesNotCommit(final FxRobot robot)
    {
        robot.interact(() -> cell.commitEditFromCurrentControl("", inputControl));

        verify(mockController, never()).setValueAtPath(anyString(), any());
    }
}
