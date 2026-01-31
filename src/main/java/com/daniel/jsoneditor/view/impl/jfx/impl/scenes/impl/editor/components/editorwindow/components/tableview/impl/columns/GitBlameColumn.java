package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl.columns;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.git.GitBlameInfo;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Table column showing git blame information (last author and commit).
 */
public class GitBlameColumn extends TableColumn<JsonNodeWithPath, String>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault());
    
    public GitBlameColumn(ReadableModel model)
    {
        super("Last Modified");
        
        setMinWidth(100);
        setPrefWidth(150);
        setSortable(false);
        
        setCellValueFactory(data -> {
            final JsonNodeWithPath nodeWithPath = data.getValue();
            final GitBlameInfo blameInfo = model.getBlameForPath(nodeWithPath.getPath());
            
            if (blameInfo != null)
            {
                return new SimpleStringProperty(blameInfo.toString());
            }
            return new SimpleStringProperty("");
        });
        
        setCellFactory(column -> new TableCell<>()
        {
            private final Rectangle colorIndicator = new Rectangle(8, 16);
            private final Label textLabel = new Label();
            private final HBox content = new HBox(5);
            
            {
                colorIndicator.setArcWidth(3);
                colorIndicator.setArcHeight(3);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setPadding(new Insets(2, 0, 2, 0));
                content.getChildren().addAll(colorIndicator, textLabel);
            }
            
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                
                if (empty || item == null || item.isEmpty())
                {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }
                
                setText(null);
                textLabel.setText(item);
                
                final JsonNodeWithPath nodeWithPath = getTableRow().getItem();
                if (nodeWithPath != null)
                {
                    final GitBlameInfo blameInfo = model.getBlameForPath(nodeWithPath.getPath());
                    if (blameInfo != null)
                    {
                        colorIndicator.setStyle("-fx-fill: " + blameInfo.getCommitColor() + ";");
                        
                        final String tooltipText = String.format(
                            "Author: %s <%s>\nCommit: %s\nDate: %s\n\n%s",
                            blameInfo.getAuthorName(),
                            blameInfo.getAuthorEmail(),
                            blameInfo.getShortCommitHash(),
                            DATE_FORMATTER.format(blameInfo.getCommitTime()),
                            blameInfo.getShortCommitMessage()
                        );
                        
                        final Tooltip tooltip = new Tooltip(tooltipText);
                        tooltip.setShowDelay(Duration.millis(300));
                        setTooltip(tooltip);
                        
                        setGraphic(content);
                        return;
                    }
                }
                
                setGraphic(null);
            }
        });
    }
}
