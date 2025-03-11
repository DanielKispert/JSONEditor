package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.cells;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.view.impl.jfx.buttons.ButtonHelper;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.EditorTableRow;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.StackPane;

public class FollowOrCreateButtonCell extends TableCell<JsonNodeWithPath, String>
{
    private final ReadableModel model;
    private final EditorWindowManager manager;
    private final StackPane pane;
    private final TableColumn<JsonNodeWithPath, String> myColumn;
    
    private String lastPath;
    
    private boolean lastEmpty;
    
    public FollowOrCreateButtonCell(ReadableModel model, EditorWindowManager manager, TableColumn<JsonNodeWithPath, String> myColumn)
    {
        this.model = model;
        this.manager = manager;
        this.myColumn = myColumn;
        pane = new StackPane();
        TableRow<JsonNodeWithPath> row = getTableRow();
        if (row instanceof EditorTableRow)
        {
            ((EditorTableRow) row).setCurrentFollowCell(this);
        }
    }
    
    @Override
    protected void updateItem(String path, boolean empty)
    {
        super.updateItem(path, empty);
        
        TableRow<JsonNodeWithPath> row = getTableRow();
        if (row instanceof EditorTableRow)
        {
            ((EditorTableRow) row).setCurrentFollowCell(this);
        }
        
        refreshCellContent(path, empty);
    }
    
    public void userEntryInSameRow()
    {
        //refreshCellContent(lastPath, lastEmpty);
    }
    
    public void refreshCellContent(String path, boolean empty)
    {
        if (empty || path == null)
        {
            setGraphic(null);
        }
        else
        {
            JsonNodeWithPath nodeAtPath = model.getNodeForPath(path);
            String referencedPath = ReferenceHelper.resolveReference(nodeAtPath, model);
            Button button;
            if (referencedPath != null)
            {
                button = makeFollowReferenceButton(referencedPath);
            }
            else
            {
                button = makeOpenArrayElementButton(path);
            }
            pane.getChildren().setAll(button);
            setGraphic(pane);
            button.widthProperty().addListener((observable, oldValue, newValue) ->
            {
                myColumn.setPrefWidth(newValue.doubleValue() + 10);
            });
        }
        this.lastPath = path;
        this.lastEmpty = empty;
    }
    
    private Button makeFollowReferenceButton(String path)
    {
        Button followReferenceButton = new Button("Follow Reference");
        followReferenceButton.setOnAction(event -> manager.openInNewWindowIfPossible(path));
        followReferenceButton.setTooltip(TooltipHelper.makeTooltipFromJsonNode(model.getNodeForPath(path).getNode()));
        followReferenceButton.setMaxHeight(Double.MAX_VALUE);
        return followReferenceButton;
    }
    
    private Button makeOpenArrayElementButton(String path)
    {
        Button openArrayElementButton = new Button();
        ButtonHelper.setButtonImage(openArrayElementButton, "/icons/material/darkmode/outline_open_in_new_white_24dp.png");
        openArrayElementButton.setOnAction(event -> manager.openInNewWindowIfPossible(path));
        openArrayElementButton.setTooltip(TooltipHelper.makeTooltipFromJsonNode(model.getNodeForPath(path).getNode()));
        openArrayElementButton.setMaxHeight(Double.MAX_VALUE);
        return openArrayElementButton;
    }
}
