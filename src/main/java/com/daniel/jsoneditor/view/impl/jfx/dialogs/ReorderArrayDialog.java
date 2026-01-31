package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;


/**
 * Dialog for reordering array elements via drag and drop.
 * Supports multi-select drag and visual drop indicators.
 */
public class ReorderArrayDialog extends ThemedDialog<List<Integer>>
{
    private static final double CELL_HEIGHT = 32.0;
    private static final double MAX_HEIGHT = 500.0;
    private static final double DEFAULT_WIDTH = 550.0;
    private static final int PREVIEW_TRUNCATE = 80;

    private final ListView<ArrayItemWrapper> listView;
    private int insertionIndex = -1;
    private List<Integer> draggingIndices = new ArrayList<>();

    // auto-scroll while dragging
    private final Timeline autoScrollTimeline;
    private double lastMouseY = -1;
    private static final double SCROLL_MARGIN = 24.0; // px near top/bottom to trigger
    private static final Duration SCROLL_INTERVAL = Duration.millis(80);
    private static final int SCROLL_STEP = 1; // rows per tick

    /**
     * Create a dialog that allows reordering the given JSON array node.
     *
     * @param arrayNode the array node to reorder
     */
    public ReorderArrayDialog(JsonNode arrayNode)
    {
        super();
        setTitle("Reorder Array");
        setHeaderText("Drag items to reorder");

        final List<ArrayItemWrapper> items = buildItems(arrayNode);

        listView = new ListView<>();
        listView.getItems().addAll(items);
        listView.getStyleClass().add("reorder-list");
        listView.setCellFactory(lv -> new ReorderCell());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setFixedCellSize(CELL_HEIGHT);
        listView.setPrefHeight(Math.min(items.size() * CELL_HEIGHT + 4, MAX_HEIGHT));
        listView.setPrefWidth(DEFAULT_WIDTH);
        listView.setMinHeight(150);
        VBox.setVgrow(listView, Priority.ALWAYS);

        // prepare auto-scroll timeline
        autoScrollTimeline = new Timeline(new KeyFrame(SCROLL_INTERVAL, ev -> handleAutoScrollTick()));
        autoScrollTimeline.setCycleCount(Timeline.INDEFINITE);

        // ListView-level drag handlers for drops on empty space / end of list
        listView.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
                // compute insertion index from mouse Y in list coords
                final double y = event.getY();
                lastMouseY = y;
                final int idx = (int) Math.floor(y / CELL_HEIGHT);
                final int newIndex = Math.max(0, Math.min(listView.getItems().size(), idx));
                if (newIndex != insertionIndex)
                {
                    insertionIndex = newIndex;
                    listView.refresh();
                }
                ensureAutoScroll();
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            final Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && insertionIndex >= 0)
            {
                final List<Integer> indices = parseIndices(db.getString());
                if (!indices.isEmpty())
                {
                    moveItems(indices, insertionIndex);
                    success = true;
                }
            }
            clearDragState();
            event.setDropCompleted(success);
            event.consume();
        });

        final Label hint = new Label("Tip: Select multiple items with Ctrl/Shift, then drag as a group");
        hint.getStyleClass().add("dialog-empty-text");

        final VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.getChildren().addAll(listView, hint);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setPrefWidth(DEFAULT_WIDTH + 40);

        setResultConverter(button -> button == ButtonType.OK ? buildNewIndices() : null);
    }

    private List<ArrayItemWrapper> buildItems(JsonNode arrayNode)
    {
        final List<ArrayItemWrapper> out = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++)
        {
            out.add(new ArrayItemWrapper(i, arrayNode.get(i)));
        }
        return out;
    }

    private List<Integer> buildNewIndices()
    {
        return listView.getItems().stream()
                .map(item -> item.originalIndex)
                .collect(Collectors.toList());
    }

    private void clearDragState()
    {
        insertionIndex = -1;
        draggingIndices.clear();
        listView.refresh();
    }

    private static List<Integer> parseIndices(String csv)
    {
        final List<Integer> out = new ArrayList<>();
        if (csv == null || csv.isBlank())
        {
            return out;
        }
        for (String part : csv.split(","))
        {
            try
            {
                out.add(Integer.parseInt(part.trim()));
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return out;
    }

    private void moveItems(List<Integer> fromIndices, int toIndex)
    {
        if (fromIndices.isEmpty())
        {
            return;
        }

        final List<ArrayItemWrapper> snapshot = new ArrayList<>(listView.getItems());
        final List<Integer> sorted = new ArrayList<>(fromIndices);
        sorted.sort(Integer::compareTo);

        final List<ArrayItemWrapper> moving = sorted.stream()
                .filter(i -> i >= 0 && i < snapshot.size())
                .map(snapshot::get)
                .collect(Collectors.toList());

        if (moving.isEmpty())
        {
            return;
        }

        int adjustedTarget = toIndex;
        for (int idx : sorted)
        {
            if (idx < toIndex)
            {
                adjustedTarget--;
            }
        }
        adjustedTarget = Math.max(0, Math.min(listView.getItems().size() - sorted.size(), adjustedTarget));

        final List<Integer> descending = new ArrayList<>(sorted);
        descending.sort(Comparator.reverseOrder());
        for (int idx : descending)
        {
            if (idx >= 0 && idx < listView.getItems().size())
            {
                listView.getItems().remove(idx);
            }
        }

        listView.getItems().addAll(adjustedTarget, moving);

        listView.getSelectionModel().clearSelection();
        for (int i = 0; i < moving.size(); i++)
        {
            listView.getSelectionModel().select(adjustedTarget + i);
        }
    }

    // -------------------- Data wrapper --------------------

    private static class ArrayItemWrapper
    {
        final int originalIndex;
        final String displayText;

        ArrayItemWrapper(int originalIndex, JsonNode node)
        {
            this.originalIndex = originalIndex;
            this.displayText = buildDisplayText(node);
        }

        private static String buildDisplayText(JsonNode node)
        {
            if (node.isObject())
            {
                return getObjectPreview(node);
            }
            else if (node.isArray())
            {
                return "[Array: " + node.size() + " items]";
            }
            else
            {
                String text = node.asText();
                return text.length() > PREVIEW_TRUNCATE ? text.substring(0, PREVIEW_TRUNCATE - 3) + "..." : text;
            }
        }

        private static String getObjectPreview(JsonNode node)
        {
            final StringBuilder sb = new StringBuilder();
            final Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext() && sb.length() < PREVIEW_TRUNCATE)
            {
                final Map.Entry<String, JsonNode> field = it.next();
                if (sb.length() > 0)
                {
                    sb.append(" | ");
                }
                if (field.getValue().isValueNode())
                {
                    sb.append(field.getKey()).append(": ").append(field.getValue().asText());
                }
            }
            String result = sb.toString();
            if (result.length() > PREVIEW_TRUNCATE)
            {
                result = result.substring(0, PREVIEW_TRUNCATE - 3) + "...";
            }
            return result.isEmpty() ? "{...}" : result;
        }
    }

    // -------------------- List cell with drag support --------------------

    private class ReorderCell extends ListCell<ArrayItemWrapper>
    {
        private final Region topIndicator;
        private final Region bottomIndicator;
        private final Label indexLabel;
        private final Label textLabel;
        private final VBox cellContainer;

        ReorderCell()
        {
            // visual drop indicators
            topIndicator = new Region();
            topIndicator.getStyleClass().add("insertion-indicator");
            topIndicator.setPrefHeight(3);
            topIndicator.setMaxHeight(3);
            topIndicator.setVisible(false);

            bottomIndicator = new Region();
            bottomIndicator.getStyleClass().add("insertion-indicator");
            bottomIndicator.setPrefHeight(3);
            bottomIndicator.setMaxHeight(3);
            bottomIndicator.setVisible(false);

            indexLabel = new Label();
            indexLabel.getStyleClass().add("index-label");
            indexLabel.setMinWidth(28);

            textLabel = new Label();
            textLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textLabel, Priority.ALWAYS);

            final HBox contentBox = new HBox(8, indexLabel, textLabel);
            contentBox.getStyleClass().add("reorder-cell-content");

            cellContainer = new VBox(topIndicator, contentBox, bottomIndicator);
            cellContainer.setFillWidth(true);

            setupDragHandlers();
        }

        private void setupDragHandlers()
        {
            // --- Drag start ---
            setOnDragDetected(event -> {
                if (getItem() == null || isEmpty())
                {
                    return;
                }

                final List<Integer> selected = new ArrayList<>(listView.getSelectionModel().getSelectedIndices());
                if (!selected.contains(getIndex()))
                {
                    selected.clear();
                    selected.add(getIndex());
                    listView.getSelectionModel().clearAndSelect(getIndex());
                }

                draggingIndices = new ArrayList<>(selected);
                draggingIndices.sort(Integer::compareTo);

                final Dragboard db = startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                content.putString(draggingIndices.stream().map(String::valueOf).collect(Collectors.joining(",")));
                db.setContent(content);

                event.consume();
            });

            // --- Drag over: determine insertion point (allow dragging over any cell including self) ---
            setOnDragOver(event -> {
                if (!event.getDragboard().hasString())
                {
                    event.consume();
                    return;
                }

                event.acceptTransferModes(TransferMode.MOVE);

                // compute mouse Y relative to listView (map scene coords)
                lastMouseY = listView.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

                // top half = insert before, bottom half = insert after
                final double y = event.getY();
                final double h = getBoundsInLocal().getHeight();
                final int newInsertionIndex = (y < h / 2) ? getIndex() : getIndex() + 1;

                if (newInsertionIndex != insertionIndex)
                {
                    insertionIndex = Math.max(0, Math.min(listView.getItems().size(), newInsertionIndex));
                    listView.refresh();
                }

                ensureAutoScroll();
                event.consume();
            });

            // --- Drop ---
            setOnDragDropped(event -> {
                final Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString() && insertionIndex >= 0)
                {
                    final List<Integer> indices = parseIndices(db.getString());
                    if (!indices.isEmpty())
                    {
                        moveItems(indices, insertionIndex);
                        success = true;
                    }
                }

                stopAutoScroll();
                clearDragState();
                event.setDropCompleted(success);
                event.consume();
            });

            // --- Drag done ---
            setOnDragDone(event -> {
                stopAutoScroll();
                clearDragState();
                event.consume();
            });
        }

        @Override
        protected void updateItem(ArrayItemWrapper item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setGraphic(null);
                setText(null);
                topIndicator.setVisible(false);
                bottomIndicator.setVisible(false);
            }
            else
            {
                indexLabel.setText(String.valueOf(item.originalIndex));
                textLabel.setText(item.displayText);

                // show insertion indicator: top if insertionIndex matches this cell, bottom only for last cell when inserting at end
                final boolean showTop = (insertionIndex == getIndex());
                final boolean showBottom = (insertionIndex == listView.getItems().size() && getIndex() == listView.getItems().size() - 1);

                topIndicator.setVisible(showTop);
                bottomIndicator.setVisible(showBottom);

                // dim if being dragged
                final boolean isDragged = draggingIndices.contains(getIndex());
                setOpacity(isDragged ? 0.4 : 1.0);

                setGraphic(cellContainer);
                setText(null);
            }
        }
    }

    private void ensureAutoScroll()
    {
        if (autoScrollTimeline == null)
        {
            return;
        }
        final double h = listView.getHeight();
        if (lastMouseY < 0)
        {
            stopAutoScroll();
            return;
        }
        if (lastMouseY < SCROLL_MARGIN || lastMouseY > h - SCROLL_MARGIN)
        {
            if (autoScrollTimeline.getStatus() != javafx.animation.Animation.Status.RUNNING)
            {
                autoScrollTimeline.play();
            }
        }
        else
        {
            stopAutoScroll();
        }
    }

    private void stopAutoScroll()
    {
        if (autoScrollTimeline != null && autoScrollTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING)
        {
            autoScrollTimeline.stop();
        }
    }

    private void handleAutoScrollTick()
    {
        if (lastMouseY < 0)
        {
            return;
        }
        final double h = listView.getHeight();
        if (lastMouseY < SCROLL_MARGIN)
        {
            // scroll up a bit
            Platform.runLater(() -> {
                // attempt to scroll up by SCROLL_STEP
                final int target = Math.max(0, insertionIndex - SCROLL_STEP);
                listView.scrollTo(target);
                // recompute insertion based on lastMouseY
                final int idx = (int) Math.floor(lastMouseY / CELL_HEIGHT);
                insertionIndex = Math.max(0, Math.min(listView.getItems().size(), idx));
                listView.refresh();
            });
        }
        else if (lastMouseY > h - SCROLL_MARGIN)
        {
            Platform.runLater(() -> {
                final int target = Math.min(listView.getItems().size() - 1, insertionIndex + SCROLL_STEP);
                listView.scrollTo(target);
                final int idx = (int) Math.floor(lastMouseY / CELL_HEIGHT);
                insertionIndex = Math.max(0, Math.min(listView.getItems().size(), idx));
                listView.refresh();
            });
         }
     }

 }
