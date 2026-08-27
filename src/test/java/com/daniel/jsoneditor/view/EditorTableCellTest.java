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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for EditorTableCell.commitEdit focus-loss behaviour: the guard must treat
 * null and "" as equivalent "empty" values to prevent spurious model writes.
 */
@ExtendWith(ApplicationExtension.class)
class EditorTableCellTest
{
    private Controller mockController;
    private TextTableCell cell;
    private Control inputControl;

    /**
     * Creates a TextTableCell backed by a required column and an empty row item
     * (the "name" property does not exist in the JSON node, committedValue == null).
     */
    @SuppressWarnings("unchecked")
    @Start
    void start(final Stage stage) throws Exception
    {
        mockController = Mockito.mock(Controller.class);
        final SettingsController mockSettings = Mockito.mock(SettingsController.class);
        Mockito.when(mockController.getSettingsController()).thenReturn(mockSettings);
        final ReadableModel mockModel = Mockito.mock(ReadableModel.class);
        final EditorWindowManager mockManager = Mockito.mock(EditorWindowManager.class);

        final EditorTableColumn mockColumn = Mockito.mock(EditorTableColumn.class);
        Mockito.when(mockColumn.isRequired()).thenReturn(true);
        Mockito.when(mockColumn.getPropertyName()).thenReturn("name");

        // ObjectNode with no "name" property — jsonNode == null inside commitEdit.
        final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        final JsonNodeWithPath mockItem = Mockito.mock(JsonNodeWithPath.class);
        Mockito.when(mockItem.getNode()).thenReturn(objectNode);
        Mockito.when(mockItem.getPath()).thenReturn("/items/0");

        final TableRow<JsonNodeWithPath> mockTableRow = Mockito.mock(TableRow.class);
        Mockito.when(mockTableRow.getItem()).thenReturn(mockItem);

        // committedValue stays null (Java default) — simulates a field never committed.
        cell = new TextTableCell(mockManager, mockController, mockModel, false, false);

        // TableCell.getTableColumn() / getTableRow() are final — they cannot be overridden
        // or mocked via subclassing. We inject mocks into the private ReadOnlyObjectWrapper
        // fields directly. Requires --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED
        // in build.gradle (field names are JavaFX-version-specific; tested against JavaFX 21).
        final Field tableRowField = TableCell.class.getDeclaredField("tableRow");
        tableRowField.setAccessible(true);
        ((ReadOnlyObjectWrapper<TableRow<JsonNodeWithPath>>) tableRowField.get(cell)).set(mockTableRow);

        // Inject column after row: updateColumnIndex() triggered by the column property's
        // invalidation calls getTableView() via the row — null tableView is safe; absent row is not.
        final Field tableColumnField = TableCell.class.getDeclaredField("tableColumn");
        tableColumnField.setAccessible(true);
        ((ReadOnlyObjectWrapper<TableColumn<JsonNodeWithPath, String>>) tableColumnField.get(cell))
                .set(mockColumn);

        inputControl = new TextField();
        final Field ctrlField = EditorTableCell.class.getDeclaredField("currentTextInputControl");
        ctrlField.setAccessible(true);
        ctrlField.set(cell, inputControl);
    }

    /** Sets the protected {@code committedValue} field on a cell via reflection. */
    private static void setCommittedValue(final TextTableCell target, final String value) throws Exception
    {
        final Field f = EditorTableCell.class.getDeclaredField("committedValue");
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Focus loss on a required field that is empty and was never committed
     * (committedValue == null) must NOT write to the model — "" on a virgin
     * field is meaningless noise, not a user edit.
     */
    @Test
    void focusLossOnEmptyRequiredFieldDoesNotCommit(final FxRobot robot)
    {
        robot.interact(() -> cell.commitEditFromCurrentControl("", inputControl));

        verify(mockController, never()).setValueAtPath(anyString(), any());
    }

    /**
     * null newValue on a never-committed field is treated identically to "" —
     * both are "empty" — and must not trigger a model write.
     */
    @Test
    void focusLossWithNullNewValueDoesNotCommit(final FxRobot robot)
    {
        robot.interact(() -> cell.commitEditFromCurrentControl(null, inputControl));

        verify(mockController, never()).setValueAtPath(anyString(), any());
    }

    /**
     * Focus loss when the user changes an existing committed value must still
     * produce a model write — the fix must not suppress legitimate edits.
     */
    @Test
    void focusLossWithChangedValueCommits(final FxRobot robot) throws Exception
    {
        setCommittedValue(cell, "old");

        robot.interact(() -> cell.commitEditFromCurrentControl("new", inputControl));

        verify(mockController).setValueAtPath("/items/0/name", "new");
    }
}