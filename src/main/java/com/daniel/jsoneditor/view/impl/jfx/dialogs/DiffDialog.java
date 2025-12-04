package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.model.diff.DiffEntry;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.EditorWindowManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;


/**
 * Dialog showing differences between two JSON trees.
 * Displays added (➕), removed (➖), and modified (✏️) entries.
 * Special markers indicate references ([REF]) and referenceable objects ([OBJ]).
 * Click on entry to navigate to path, use revert button to restore saved value.
 */
public class DiffDialog extends Dialog<Void>
{
    private final ListView<DiffEntry> diffListView;
    private final EditorWindowManager editorWindowManager;
    private final Controller controller;
    
    public DiffDialog(List<DiffEntry> diffs, EditorWindowManager editorWindowManager, Controller controller)
    {
        if (diffs == null)
        {
            throw new IllegalArgumentException("Diffs list cannot be null");
        }
        
        this.editorWindowManager = editorWindowManager;
        this.controller = controller;
        
        setTitle(String.format("JSON Differences (%d)", diffs.size()));
        
        final BorderPane content = new BorderPane();
        content.setPrefSize(800, 600);
        
        diffListView = createDiffListView(diffs);
        content.setCenter(diffListView);
        
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style_darkmode.css").toExternalForm()
        );
    }
    
    private ListView<DiffEntry> createDiffListView(List<DiffEntry> diffs)
    {
        final ListView<DiffEntry> listView = new ListView<>();
        listView.getItems().addAll(diffs);
        listView.setCellFactory(param -> new DiffCell(editorWindowManager, controller, this));
        
        // TODO: Re-enable navigation on double-click later
        /*
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
            {
                final DiffEntry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null)
                {
                    navigateToPath(selected.getPath());
                }
            }
        });
        */
        
        HBox.setHgrow(listView, Priority.ALWAYS);
        return listView;
    }
    
    private void navigateToPath(String path)
    {
        editorWindowManager.openPath(path);
        close();
    }
    
    private static class DiffCell extends ListCell<DiffEntry>
    {
        private final EditorWindowManager editorWindowManager;
        private final Controller controller;
        private final DiffDialog dialog;
        
        public DiffCell(EditorWindowManager editorWindowManager, Controller controller, DiffDialog dialog)
        {
            this.editorWindowManager = editorWindowManager;
            this.controller = controller;
            this.dialog = dialog;
        }
        
        @Override
        protected void updateItem(DiffEntry item, boolean empty)
        {
            super.updateItem(item, empty);
            
            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
                return;
            }
            
            final BorderPane cellContent = new BorderPane();
            cellContent.setPadding(new Insets(8, 10, 8, 10));
            
            final HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);
            
            final Text iconText = new Text(getIconForType(item.getType()));
            iconText.getStyleClass().add("diff-icon-" + item.getType().name().toLowerCase());
            
            final Text pathText = new Text(item.getPath());
            pathText.getStyleClass().add("dialog-list-cell-text");
            
            topRow.getChildren().addAll(iconText, pathText);
            
            if (item.getEntryType() != DiffEntry.EntryType.NORMAL)
            {
                final Label typeIndicator = new Label(" [" + getEntryTypeLabel(item.getEntryType()) + "]");
                typeIndicator.getStyleClass().add("diff-entry-type-indicator");
                typeIndicator.setTooltip(new Tooltip(getEntryTypeTooltip(item.getEntryType())));
                topRow.getChildren().add(typeIndicator);
            }
            
            // TODO: Re-enable revert button later
            /*
            if (item.getType() == DiffEntry.DiffType.MODIFIED || item.getType() == DiffEntry.DiffType.ADDED)
            {
                final Button revertButton = new Button("⟲");
                revertButton.getStyleClass().add("small-button");
                revertButton.setTooltip(new Tooltip("Revert to saved value"));
                revertButton.setOnAction(event -> {
                    event.consume();
                    revertEntry(item);
                });
                
                final HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                topRow.getChildren().addAll(spacer, revertButton);
            }
            */
            
            cellContent.setTop(topRow);
            
            final TextFlow valueFlow = new TextFlow();
            final Text valueText = new Text(item.getDisplayValue());
            valueText.getStyleClass().add("dialog-list-cell-extra");
            valueFlow.getChildren().add(valueText);
            valueFlow.setPadding(new Insets(5, 0, 0, 25));
            
            cellContent.setCenter(valueFlow);
            
            setGraphic(cellContent);
        }
        
        private void revertEntry(DiffEntry item)
        {
            if (item.getType() == DiffEntry.DiffType.MODIFIED)
            {
                controller.overrideNodeAtPath(item.getPath(), item.getOldValue());
            }
            else if (item.getType() == DiffEntry.DiffType.ADDED)
            {
                controller.removeNodes(java.util.Collections.singletonList(item.getPath()));
            }
            
            dialog.close();
        }
        
        private String getIconForType(DiffEntry.DiffType type)
        {
            switch (type)
            {
                case ADDED:
                    return "➕";
                case REMOVED:
                    return "➖";
                case MODIFIED:
                    return "✏️";
                default:
                    return "•";
            }
        }
        
        private String getEntryTypeLabel(DiffEntry.EntryType entryType)
        {
            switch (entryType)
            {
                case REFERENCE_TO_OBJECT:
                    return "REF";
                case REFERENCEABLE_OBJECT:
                    return "OBJ";
                default:
                    return "";
            }
        }
        
        private String getEntryTypeTooltip(DiffEntry.EntryType entryType)
        {
            switch (entryType)
            {
                case REFERENCE_TO_OBJECT:
                    return "Entity Reference";
                case REFERENCEABLE_OBJECT:
                    return "Referenceable Object";
                default:
                    return "";
            }
        }
    }
}

